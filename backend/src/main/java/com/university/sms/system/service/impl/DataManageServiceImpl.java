package com.university.sms.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.university.sms.common.exception.BusinessException;
import com.university.sms.course.entity.Course;
import com.university.sms.course.mapper.CourseMapper;
import com.university.sms.grade.entity.Grade;
import com.university.sms.grade.mapper.GradeMapper;
import com.university.sms.leave.entity.LeaveRequest;
import com.university.sms.leave.mapper.LeaveRequestMapper;
import com.university.sms.student.entity.Student;
import com.university.sms.student.mapper.StudentMapper;
import com.university.sms.system.dto.RestoreStatusDTO;
import com.university.sms.system.entity.DictItem;
import com.university.sms.system.entity.DictType;
import com.university.sms.system.mapper.DictItemMapper;
import com.university.sms.system.mapper.DictTypeMapper;
import com.university.sms.system.service.DataManageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataManageServiceImpl implements DataManageService {

    private static final ExecutorService RESTORE_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "backup-restore");
        t.setDaemon(false);
        return t;
    });

    private volatile RestoreStatusDTO restoreStatus = RestoreStatusDTO.idle();

    @Value("${app.backup.path:/app/backups}")
    private String backupPath;

    @Value("${DB_HOST:mysql}")
    private String dbHost;

    @Value("${DB_PORT:3306}")
    private String dbPort;

    @Value("${DB_NAME:sms_db}")
    private String dbName;

    @Value("${DB_USER:root}")
    private String dbUser;

    @Value("${DB_PASSWORD:root123}")
    private String dbPassword;

    private final StudentMapper studentMapper;
    private final CourseMapper courseMapper;
    private final GradeMapper gradeMapper;
    private final LeaveRequestMapper leaveRequestMapper;
    private final DictTypeMapper dictTypeMapper;
    private final DictItemMapper dictItemMapper;

    // ==================== 备份管理 ====================

    @Override
    public List<Map<String, Object>> getBackupList() {
        List<Map<String, Object>> backups = new ArrayList<>();
        File backupDir = new File(backupPath);

        if (backupDir.exists() && backupDir.isDirectory()) {
            File[] files = backupDir.listFiles((dir, name) -> name.endsWith(".sql"));
            if (files != null) {
                for (File file : files) {
                    Map<String, Object> backup = new HashMap<>();
                    backup.put("fileName", file.getName());
                    backup.put("fileSize", formatFileSize(file.length()));
                    backup.put("createdAt", new Date(file.lastModified()));
                    backups.add(backup);
                }
            }
        }

        backups.sort((a, b) -> ((Date) b.get("createdAt")).compareTo((Date) a.get("createdAt")));
        return backups;
    }

    @Override
    public Map<String, Object> createBackup() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "backup_" + timestamp + ".sql";

        File backupDir = new File(backupPath);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }

        File backupFile = new File(backupDir, fileName);

        try {
            exportDatabaseToFile(backupFile);
            log.info("创建数据备份成功: {}, 大小: {}", fileName, formatFileSize(backupFile.length()));

            Map<String, Object> result = new HashMap<>();
            result.put("fileName", fileName);
            result.put("fileSize", formatFileSize(backupFile.length()));
            result.put("message", "备份创建成功，文件保存在服务器 " + backupPath + " 目录");
            return result;
        } catch (Exception e) {
            log.error("创建备份失败", e);
            if (backupFile.exists()) {
                backupFile.delete();
            }
            throw new BusinessException("创建备份失败: " + e.getMessage());
        }
    }

    @Override
    public byte[] getBackupFileContent(String fileName) {
        validateFileName(fileName);
        File backupFile = new File(backupPath, fileName);
        if (!backupFile.exists()) {
            throw new BusinessException("备份文件不存在");
        }
        try {
            return Files.readAllBytes(backupFile.toPath());
        } catch (IOException e) {
            log.error("读取备份文件失败", e);
            throw new BusinessException("读取备份文件失败: " + e.getMessage());
        }
    }

    @Override
    public String getBackupFileName(String fileName) {
        validateFileName(fileName);
        File backupFile = new File(backupPath, fileName);
        if (!backupFile.exists()) {
            throw new BusinessException("备份文件不存在");
        }
        return fileName;
    }

    @Override
    public void deleteBackup(String fileName) {
        validateFileName(fileName);
        File backupFile = new File(backupPath, fileName);
        if (!backupFile.exists() || !backupFile.delete()) {
            throw new BusinessException("删除备份失败");
        }
        log.info("删除备份: {}", fileName);
    }

    @Override
    public RestoreStatusDTO getRestoreStatus() {
        RestoreStatusDTO current = restoreStatus;
        return current != null ? current : RestoreStatusDTO.idle();
    }

    @Override
    public void startRestoreAsync(String fileName) {
        if ("RUNNING".equals(restoreStatus.getStatus())) {
            throw new BusinessException("已有恢复任务进行中，请稍后再试");
        }
        validateFileName(fileName);
        File backupFile = new File(backupPath, fileName);
        if (!backupFile.exists()) {
            throw new BusinessException("备份文件不存在");
        }
        restoreStatus = RestoreStatusDTO.running();
        String fileToRestore = fileName;
        RESTORE_EXECUTOR.submit(() -> {
            try {
                doRestoreBackup(fileToRestore);
                restoreStatus = RestoreStatusDTO.success("数据已成功从备份恢复");
                log.warn("数据库已成功从备份 {} 恢复", fileToRestore);
            } catch (Exception e) {
                log.error("从备份恢复数据库失败", e);
                restoreStatus = RestoreStatusDTO.failed("恢复失败: " + e.getMessage());
            }
        });
    }

    @Override
    @Transactional
    public void restoreBackup(String fileName) {
        doRestoreBackup(fileName);
    }

    private void doRestoreBackup(String fileName) {
        validateFileName(fileName);
        File backupFile = new File(backupPath, fileName);
        if (!backupFile.exists()) {
            throw new BusinessException("备份文件不存在");
        }

        log.warn("开始从备份文件恢复数据库: {}", fileName);

        String jdbcUrl = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName
                + "?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true&allowMultiQueries=true";

        try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
             Statement stmt = conn.createStatement();
             BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(backupFile), java.nio.charset.StandardCharsets.UTF_8))) {

            conn.setAutoCommit(false);

            StringBuilder sqlBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("--") || trimmed.startsWith("/*")) {
                    continue;
                }
                sqlBuilder.append(trimmed).append("\n");
                if (trimmed.endsWith(";")) {
                    String sql = sqlBuilder.toString().trim();
                    sqlBuilder.setLength(0);
                    sql = sql.replaceFirst(";+\\s*$", ";");
                    if (!sql.isEmpty() && !";".equals(sql)) {
                        stmt.execute(sql);
                    }
                }
            }

            conn.commit();
        } catch (Exception e) {
            log.error("从备份恢复数据库失败", e);
            throw new BusinessException("恢复备份失败: " + e.getMessage());
        }
    }

    // ==================== 数据导出 ====================

    @Override
    public void exportData(String type, OutputStream outputStream) {
        try {
            outputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);

            switch (type) {
                case "student" -> exportStudents(writer);
                case "course" -> exportCourses(writer);
                case "grade" -> exportGrades(writer);
                case "leave" -> exportLeaveRequests(writer);
                default -> throw new BusinessException("不支持的导出类型: " + type);
            }

            writer.flush();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("导出数据失败", e);
            throw new BusinessException("导出失败: " + e.getMessage());
        }
    }

    // ==================== 数据字典 ====================

    @Override
    public List<Map<String, Object>> getDataDictionary() {
        List<Map<String, Object>> dictionary = new ArrayList<>();

        List<DictType> dictTypes = dictTypeMapper.selectList(
                new LambdaQueryWrapper<DictType>()
                        .eq(DictType::getStatus, 1)
                        .orderByAsc(DictType::getSortOrder));

        for (DictType type : dictTypes) {
            List<DictItem> items = dictItemMapper.selectByDictCode(type.getDictCode());

            List<Map<String, String>> values = new ArrayList<>();
            for (DictItem item : items) {
                Map<String, String> value = new HashMap<>();
                value.put("value", item.getItemValue());
                value.put("label", item.getItemLabel());
                value.put("cssClass", item.getCssClass());
                values.add(value);
            }

            Map<String, Object> dictEntry = new HashMap<>();
            dictEntry.put("code", type.getDictCode());
            dictEntry.put("name", type.getDictName());
            dictEntry.put("description", type.getDescription());
            dictEntry.put("isSystem", type.getIsSystem());
            dictEntry.put("values", values);
            dictionary.add(dictEntry);
        }

        return dictionary;
    }

    @Override
    public List<DictType> getDictTypes() {
        return dictTypeMapper.selectList(
                new LambdaQueryWrapper<DictType>()
                        .eq(DictType::getStatus, 1)
                        .orderByAsc(DictType::getSortOrder));
    }

    @Override
    @Transactional
    public DictType createDictType(DictType dictType) {
        DictType existing = dictTypeMapper.selectOne(
                new LambdaQueryWrapper<DictType>().eq(DictType::getDictCode, dictType.getDictCode()));
        if (existing != null) {
            throw new BusinessException("字典编码已存在");
        }

        dictType.setIsSystem(0);
        dictType.setStatus(1);
        dictTypeMapper.insert(dictType);
        return dictType;
    }

    @Override
    @Transactional
    public void updateDictType(Long id, DictType dictType) {
        DictType existing = dictTypeMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("字典类型不存在");
        }
        if (existing.getIsSystem() == 1) {
            throw new BusinessException("系统内置字典不允许修改");
        }

        dictType.setId(id);
        dictType.setDictCode(existing.getDictCode());
        dictTypeMapper.updateById(dictType);
    }

    @Override
    @Transactional
    public void deleteDictType(Long id) {
        DictType existing = dictTypeMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("字典类型不存在");
        }
        if (existing.getIsSystem() == 1) {
            throw new BusinessException("系统内置字典不允许删除");
        }

        dictItemMapper.delete(new LambdaQueryWrapper<DictItem>()
                .eq(DictItem::getDictCode, existing.getDictCode()));
        dictTypeMapper.deleteById(id);
    }

    @Override
    public List<DictItem> getDictItems(String dictCode) {
        return dictItemMapper.selectByDictCode(dictCode);
    }

    @Override
    @Transactional
    public DictItem createDictItem(DictItem dictItem) {
        DictType dictType = dictTypeMapper.selectOne(
                new LambdaQueryWrapper<DictType>().eq(DictType::getDictCode, dictItem.getDictCode()));
        if (dictType == null) {
            throw new BusinessException("字典类型不存在");
        }

        DictItem existing = dictItemMapper.selectOne(
                new LambdaQueryWrapper<DictItem>()
                        .eq(DictItem::getDictCode, dictItem.getDictCode())
                        .eq(DictItem::getItemValue, dictItem.getItemValue()));
        if (existing != null) {
            throw new BusinessException("字典项值已存在");
        }

        dictItem.setDictTypeId(dictType.getId());
        dictItem.setStatus(1);
        dictItemMapper.insert(dictItem);
        return dictItem;
    }

    @Override
    @Transactional
    public void updateDictItem(Long id, DictItem dictItem) {
        DictItem existing = dictItemMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("字典项不存在");
        }

        DictType dictType = dictTypeMapper.selectOne(
                new LambdaQueryWrapper<DictType>().eq(DictType::getDictCode, existing.getDictCode()));
        if (dictType != null && dictType.getIsSystem() == 1) {
            throw new BusinessException("系统内置字典项不允许修改");
        }

        dictItem.setId(id);
        dictItem.setDictCode(existing.getDictCode());
        dictItem.setItemValue(existing.getItemValue());
        dictItemMapper.updateById(dictItem);
    }

    @Override
    @Transactional
    public void deleteDictItem(Long id) {
        DictItem existing = dictItemMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("字典项不存在");
        }

        DictType dictType = dictTypeMapper.selectOne(
                new LambdaQueryWrapper<DictType>().eq(DictType::getDictCode, existing.getDictCode()));
        if (dictType != null && dictType.getIsSystem() == 1) {
            throw new BusinessException("系统内置字典项不允许删除");
        }

        dictItemMapper.deleteById(id);
    }

    // ==================== 私有方法 ====================

    private void validateFileName(String fileName) {
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new BusinessException("非法的文件名");
        }
    }

    private void exportDatabaseToFile(File backupFile) throws Exception {
        try (FileWriter writer = new FileWriter(backupFile);
             Connection conn = DriverManager.getConnection(
                     "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName +
                             "?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true",
                     dbUser, dbPassword)) {

            writer.write("-- Student Management System Database Backup\n");
            writer.write("-- Generated at: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
            writer.write("-- Database: " + dbName + "\n\n");
            writer.write("SET NAMES utf8mb4;\n");
            writer.write("SET FOREIGN_KEY_CHECKS = 0;\n\n");

            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(dbName, null, "%", new String[]{"TABLE"});

            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                exportTable(conn, writer, tableName);
            }

            writer.write("\nSET FOREIGN_KEY_CHECKS = 1;\n");
            writer.flush();
        }
    }

    private void exportTable(Connection conn, FileWriter writer, String tableName) throws Exception {
        writer.write("-- ----------------------------\n");
        writer.write("-- Table structure for " + tableName + "\n");
        writer.write("-- ----------------------------\n");
        writer.write("DROP TABLE IF EXISTS `" + tableName + "`;\n");

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW CREATE TABLE `" + tableName + "`")) {
            if (rs.next()) {
                String createTable = rs.getString(2);
                writer.write(createTable + ";\n\n");
            }
        }

        writer.write("-- ----------------------------\n");
        writer.write("-- Records of " + tableName + "\n");
        writer.write("-- ----------------------------\n");

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM `" + tableName + "`")) {

            ResultSetMetaData rsMetaData = rs.getMetaData();
            int columnCount = rsMetaData.getColumnCount();

            while (rs.next()) {
                StringBuilder sb = new StringBuilder();
                sb.append("INSERT INTO `").append(tableName).append("` VALUES (");

                for (int i = 1; i <= columnCount; i++) {
                    if (i > 1) sb.append(", ");

                    Object value = rs.getObject(i);
                    if (value == null) {
                        sb.append("NULL");
                    } else if (value instanceof Number) {
                        sb.append(value);
                    } else if (value instanceof byte[]) {
                        sb.append("NULL");
                    } else {
                        String strValue = value.toString()
                                .replace("\\", "\\\\")
                                .replace("'", "\\'")
                                .replace("\n", "\\n")
                                .replace("\r", "\\r");
                        sb.append("'").append(strValue).append("'");
                    }
                }

                sb.append(");\n");
                writer.write(sb.toString());
            }
        }

        writer.write("\n");
    }

    private void exportStudents(PrintWriter writer) {
        writer.println("学号,姓名,性别,院系,专业,班级,学籍状态,入学日期,联系电话,邮箱");

        List<Student> students = studentMapper.selectList(
                new LambdaQueryWrapper<Student>().eq(Student::getDeleted, 0));

        for (Student s : students) {
            String gender = "M".equals(s.getGender()) ? "男" : "女";
            String status = translateAcademicStatus(s.getAcademicStatus());
            String enrollDate = s.getEnrollmentDate() != null ? s.getEnrollmentDate().toString() : "";

            writer.println(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                    escapeCSV(s.getStudentNo()),
                    escapeCSV(s.getName()),
                    gender,
                    escapeCSV(s.getDepartment()),
                    escapeCSV(s.getMajor()),
                    escapeCSV(s.getClassNo()),
                    status,
                    enrollDate,
                    escapeCSV(s.getPhone()),
                    escapeCSV(s.getEmail())
            ));
        }
    }

    private void exportCourses(PrintWriter writer) {
        writer.println("课程编号,课程名称,学分,授课教师,上课时间,上课地点,课程类别,容量,已选人数,学期");

        List<Course> courses = courseMapper.selectList(
                new LambdaQueryWrapper<Course>().eq(Course::getDeleted, 0));

        for (Course c : courses) {
            String category = translateCourseCategory(c.getCategory());

            writer.println(String.format("%s,%s,%s,%s,%s,%s,%s,%d,%d,%s",
                    escapeCSV(c.getCourseCode()),
                    escapeCSV(c.getName()),
                    c.getCredits(),
                    escapeCSV(c.getTeacherName()),
                    escapeCSV(c.getSchedule()),
                    escapeCSV(c.getLocation()),
                    category,
                    c.getCapacity() != null ? c.getCapacity() : 0,
                    c.getEnrolledCount() != null ? c.getEnrolledCount() : 0,
                    escapeCSV(c.getSemester())
            ));
        }
    }

    private void exportGrades(PrintWriter writer) {
        writer.println("学号,姓名,课程编号,课程名称,成绩,等级,绩点,状态,学期");

        List<Grade> grades = gradeMapper.selectList(null);

        for (Grade g : grades) {
            Student student = studentMapper.selectById(g.getStudentId());
            Course course = courseMapper.selectById(g.getCourseId());

            if (student != null && course != null) {
                String status = translateGradeStatus(g.getStatus());

                writer.println(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s",
                        escapeCSV(student.getStudentNo()),
                        escapeCSV(student.getName()),
                        escapeCSV(course.getCourseCode()),
                        escapeCSV(course.getName()),
                        g.getScore() != null ? g.getScore().toString() : "",
                        escapeCSV(g.getLetterGrade()),
                        g.getGradePoints() != null ? g.getGradePoints().toString() : "",
                        status,
                        escapeCSV(g.getSemester())
                ));
            }
        }
    }

    private void exportLeaveRequests(PrintWriter writer) {
        writer.println("学号,姓名,请假类型,开始日期,结束日期,天数,请假原因,状态,销假日期");

        List<LeaveRequest> leaves = leaveRequestMapper.selectList(null);

        for (LeaveRequest l : leaves) {
            Student student = studentMapper.selectById(l.getStudentId());

            if (student != null) {
                String leaveType = translateLeaveType(l.getLeaveType());
                String status = translateLeaveStatus(l.getStatus());
                String returnDate = l.getReturnDate() != null ? l.getReturnDate().toString() : "";

                writer.println(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s",
                        escapeCSV(student.getStudentNo()),
                        escapeCSV(student.getName()),
                        leaveType,
                        l.getStartDate(),
                        l.getEndDate(),
                        l.getDuration(),
                        escapeCSV(l.getReason()),
                        status,
                        returnDate
                ));
            }
        }
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String translateAcademicStatus(String status) {
        if (status == null) return "";
        return switch (status) {
            case "ENROLLED" -> "在读";
            case "SUSPENDED" -> "休学";
            case "WITHDRAWN" -> "退学";
            case "GRADUATED" -> "毕业";
            case "TRANSFERRED" -> "转学";
            default -> status;
        };
    }

    private String translateCourseCategory(String category) {
        if (category == null) return "";
        return switch (category) {
            case "REQUIRED" -> "必修";
            case "ELECTIVE" -> "选修";
            case "GENERAL" -> "通识";
            default -> category;
        };
    }

    private String translateGradeStatus(String status) {
        if (status == null) return "";
        return switch (status) {
            case "DRAFT" -> "草稿";
            case "SUBMITTED" -> "已提交";
            case "APPROVED" -> "已审批";
            default -> status;
        };
    }

    private String translateLeaveType(String type) {
        if (type == null) return "";
        return switch (type) {
            case "SICK" -> "病假";
            case "PERSONAL" -> "事假";
            case "OFFICIAL" -> "公假";
            default -> type;
        };
    }

    private String translateLeaveStatus(String status) {
        if (status == null) return "";
        return switch (status) {
            case "PENDING" -> "待审批";
            case "COUNSELOR_APPROVED" -> "辅导员已批";
            case "APPROVED" -> "已批准";
            case "REJECTED" -> "已拒绝";
            case "COMPLETED" -> "已销假";
            default -> status;
        };
    }

    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.2f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.2f MB", size / (1024.0 * 1024));
        return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
    }
}
