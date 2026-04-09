package com.university.sms.system.controller;

import com.university.sms.common.response.Result;
import com.university.sms.security.annotation.RequirePermission;
import com.university.sms.system.dto.RestoreStatusDTO;
import com.university.sms.system.entity.DictItem;
import com.university.sms.system.entity.DictType;
import com.university.sms.system.service.DataManageService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/system/data")
@RequiredArgsConstructor
public class DataManageController {

    private final DataManageService dataManageService;

    // ==================== 备份管理 ====================

    @GetMapping("/backups")
    @RequirePermission("backup:create")
    public Result<List<Map<String, Object>>> getBackupList() {
        return Result.success(dataManageService.getBackupList());
    }

    @PostMapping("/backup")
    @RequirePermission("backup:create")
    public Result<Map<String, Object>> createBackup() {
        return Result.success(dataManageService.createBackup());
    }

    @GetMapping("/backup/download/{fileName}")
    @RequirePermission("backup:create")
    public void downloadBackup(@PathVariable String fileName, HttpServletResponse response) {
        byte[] content = dataManageService.getBackupFileContent(fileName);
        String validName = dataManageService.getBackupFileName(fileName);

        try {
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" +
                    URLEncoder.encode(validName, StandardCharsets.UTF_8));
            response.setContentLengthLong(content.length);
            response.getOutputStream().write(content);
            response.getOutputStream().flush();
            log.info("下载备份文件: {}", validName);
        } catch (Exception e) {
            log.error("下载备份文件失败", e);
            throw new RuntimeException("下载失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/backup/{fileName}")
    @RequirePermission("backup:create")
    public Result<Void> deleteBackup(@PathVariable String fileName) {
        dataManageService.deleteBackup(fileName);
        return Result.success();
    }

    @PostMapping("/backup/restore/{fileName}")
    @RequirePermission("backup:create")
    public Result<Void> restoreBackup(@PathVariable String fileName) {
        dataManageService.startRestoreAsync(fileName);
        return Result.success();
    }

    /**
     * 恢复状态轮询（与 restore/{fileName} 区分，避免 pathVariable 把 status 当文件名）。
     * 故意不做 @RequirePermission：恢复过程中会 DROP/CREATE 表，若此处查权限会查 sys_permission 等表导致 500；
     * 仅要求已登录即可轮询状态，发起恢复仍校验 backup:create。
     */
    @GetMapping("/backup/restore-status")
    public Result<RestoreStatusDTO> getRestoreStatus() {
        return Result.success(dataManageService.getRestoreStatus());
    }

    // ==================== 数据导出 ====================

    @GetMapping("/export/{type}")
    @RequirePermission("student:export")
    public void exportData(@PathVariable String type, HttpServletResponse response) {
        String typeName = switch (type) {
            case "student" -> "学生数据";
            case "course" -> "课程数据";
            case "grade" -> "成绩数据";
            case "leave" -> "请假记录";
            default -> "数据";
        };
        String fileName = type + "_export_" + System.currentTimeMillis() + ".csv";

        try {
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" +
                    URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            dataManageService.exportData(type, response.getOutputStream());
            log.info("导出{}成功: {}", typeName, fileName);
        } catch (Exception e) {
            log.error("导出数据失败", e);
            throw new RuntimeException("导出失败: " + e.getMessage());
        }
    }

    // ==================== 数据字典 ====================

    @GetMapping("/dictionary")
    @RequirePermission("config:view")
    public Result<List<Map<String, Object>>> getDataDictionary() {
        return Result.success(dataManageService.getDataDictionary());
    }

    @GetMapping("/dictionary/types")
    @RequirePermission("config:view")
    public Result<List<DictType>> getDictTypes() {
        return Result.success(dataManageService.getDictTypes());
    }

    @PostMapping("/dictionary/types")
    @RequirePermission("config:update")
    public Result<DictType> createDictType(@RequestBody DictType dictType) {
        return Result.success(dataManageService.createDictType(dictType));
    }

    @PutMapping("/dictionary/types/{id}")
    @RequirePermission("config:update")
    public Result<Void> updateDictType(@PathVariable Long id, @RequestBody DictType dictType) {
        dataManageService.updateDictType(id, dictType);
        return Result.success();
    }

    @DeleteMapping("/dictionary/types/{id}")
    @RequirePermission("config:update")
    public Result<Void> deleteDictType(@PathVariable Long id) {
        dataManageService.deleteDictType(id);
        return Result.success();
    }

    @GetMapping("/dictionary/items/{dictCode}")
    @RequirePermission("config:view")
    public Result<List<DictItem>> getDictItems(@PathVariable String dictCode) {
        return Result.success(dataManageService.getDictItems(dictCode));
    }

    @PostMapping("/dictionary/items")
    @RequirePermission("config:update")
    public Result<DictItem> createDictItem(@RequestBody DictItem dictItem) {
        return Result.success(dataManageService.createDictItem(dictItem));
    }

    @PutMapping("/dictionary/items/{id}")
    @RequirePermission("config:update")
    public Result<Void> updateDictItem(@PathVariable Long id, @RequestBody DictItem dictItem) {
        dataManageService.updateDictItem(id, dictItem);
        return Result.success();
    }

    @DeleteMapping("/dictionary/items/{id}")
    @RequirePermission("config:update")
    public Result<Void> deleteDictItem(@PathVariable Long id) {
        dataManageService.deleteDictItem(id);
        return Result.success();
    }
}
