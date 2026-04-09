package com.university.sms.common.controller;

import com.university.sms.common.response.Result;
import com.university.sms.leave.entity.LeaveAttachment;
import com.university.sms.leave.mapper.LeaveAttachmentMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 文件上传控制器
 */
@Slf4j
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {
    
    @Value("${app.upload.path:/app/uploads}")
    private String uploadPath;
    
    private final LeaveAttachmentMapper leaveAttachmentMapper;
    
    // 允许的文件类型
    private static final Set<String> ALLOWED_TYPES = Set.of(
        "image/jpeg", "image/png", "image/gif",
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );
    
    // 最大文件大小 10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    
    /**
     * 上传请假附件
     */
    @PostMapping("/leave/{leaveId}")
    public Result<Map<String, Object>> uploadLeaveAttachment(
            @PathVariable Long leaveId,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        
        Long userId = (Long) request.getAttribute("userId");
        
        // 验证文件
        if (file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            return Result.error("文件大小不能超过10MB");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            return Result.error("不支持的文件类型，仅支持图片、PDF和Word文档");
        }
        
        try {
            // 生成存储路径
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String uploadDir = uploadPath + "/leave/" + dateDir;
            
            // 确保目录存在
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                log.info("创建上传目录: {}, 结果: {}", uploadDir, created);
            }
            
            // 生成唯一文件名
            String originalName = file.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }
            String newFileName = UUID.randomUUID().toString() + extension;
            String filePath = uploadDir + "/" + newFileName;
            
            // 保存文件
            File destFile = new File(filePath);
            file.transferTo(destFile);
            log.info("文件保存成功: {}", filePath);
            
            // 保存附件记录
            LeaveAttachment attachment = new LeaveAttachment();
            attachment.setLeaveRequestId(leaveId);
            attachment.setFileName(originalName);
            attachment.setFilePath("leave/" + dateDir + "/" + newFileName);
            attachment.setFileSize(file.getSize());
            attachment.setFileType(contentType);
            attachment.setUploadedBy(userId);
            leaveAttachmentMapper.insert(attachment);
            
            log.info("上传请假附件成功: leaveId={}, fileName={}", leaveId, originalName);
            
            Map<String, Object> result = new HashMap<>();
            result.put("id", attachment.getId());
            result.put("fileName", originalName);
            result.put("fileSize", formatFileSize(file.getSize()));
            result.put("filePath", attachment.getFilePath());
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("上传文件失败", e);
            return Result.error("上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取请假附件列表
     */
    @GetMapping("/leave/{leaveId}")
    public Result<List<Map<String, Object>>> getLeaveAttachments(@PathVariable Long leaveId) {
        List<LeaveAttachment> attachments = leaveAttachmentMapper.selectByLeaveRequestId(leaveId);
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (LeaveAttachment att : attachments) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", att.getId());
            item.put("fileName", att.getFileName());
            item.put("fileSize", formatFileSize(att.getFileSize()));
            item.put("fileType", att.getFileType());
            item.put("createdAt", att.getCreatedAt());
            result.add(item);
        }
        
        return Result.success(result);
    }
    
    /**
     * 下载附件
     */
    @GetMapping("/download/{id}")
    public void downloadAttachment(@PathVariable Long id, HttpServletResponse response) {
        LeaveAttachment attachment = leaveAttachmentMapper.selectById(id);
        if (attachment == null) {
            throw new RuntimeException("附件不存在");
        }
        
        File file = new File(uploadPath + "/" + attachment.getFilePath());
        if (!file.exists()) {
            throw new RuntimeException("文件不存在");
        }
        
        try {
            response.setContentType(attachment.getFileType());
            response.setHeader("Content-Disposition", "attachment; filename=" + 
                java.net.URLEncoder.encode(attachment.getFileName(), "UTF-8"));
            response.setContentLengthLong(file.length());
            
            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }
        } catch (Exception e) {
            log.error("下载文件失败", e);
            throw new RuntimeException("下载失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除附件
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteAttachment(@PathVariable Long id) {
        LeaveAttachment attachment = leaveAttachmentMapper.selectById(id);
        if (attachment == null) {
            return Result.error("附件不存在");
        }
        
        // 删除文件
        File file = new File(uploadPath + "/" + attachment.getFilePath());
        if (file.exists()) {
            file.delete();
        }
        
        // 删除记录
        leaveAttachmentMapper.deleteById(id);
        log.info("删除附件: id={}, fileName={}", id, attachment.getFileName());
        
        return Result.success();
    }
    
    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.2f KB", size / 1024.0);
        return String.format("%.2f MB", size / (1024.0 * 1024));
    }
}
