package com.university.academic.controller;

import com.university.academic.service.FileStorageService;
import com.university.academic.vo.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传Controller
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileUploadController {
    
    private final FileStorageService fileStorageService;
    
    /**
     * 上传奖学金申请附件
     */
    @PostMapping("/scholarship-attachment")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<Map<String, String>> uploadScholarshipAttachment(@RequestParam("file") MultipartFile file) {
        log.info("上传奖学金申请附件: filename={}, size={}", file.getOriginalFilename(), file.getSize());
        
        // 验证文件大小（最大10MB）
        if (file.getSize() > 10 * 1024 * 1024) {
            return Result.error(400, "文件大小不能超过10MB");
        }
        
        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/") && 
                                    !contentType.equals("application/pdf") &&
                                    !contentType.startsWith("application/vnd.openxmlformats"))) {
            return Result.error(400, "只支持上传图片、PDF或Office文档");
        }
        
        try {
            String fileUrl = fileStorageService.storeFile(file, "scholarship");
            
            Map<String, String> response = new HashMap<>();
            response.put("url", fileUrl);
            response.put("filename", file.getOriginalFilename());
            
            return Result.success(response);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error(500, "文件上传失败: " + e.getMessage());
        }
    }
}

