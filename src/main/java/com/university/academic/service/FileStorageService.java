package com.university.academic.service;

import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 文件存储服务
 */
@Service
@Slf4j
public class FileStorageService {
    
    @Value("${file.upload.path:uploads}")
    private String uploadPath;
    
    @Value("${file.upload.base-url:http://localhost:8080/uploads}")
    private String baseUrl;
    
    /**
     * 存储文件
     *
     * @param file 文件
     * @param category 类别（用于分类存储）
     * @return 文件访问URL
     */
    public String storeFile(MultipartFile file, String category) {
        try {
            // 验证文件名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.contains("..")) {
                throw new BusinessException(ErrorCode.FILE_NAME_INVALID);
            }
            
            // 生成唯一文件名
            String extension = StringUtils.getFilenameExtension(originalFilename);
            String newFilename = UUID.randomUUID().toString() + "." + extension;
            
            // 创建目录结构：uploads/category/yyyy/MM/dd/
            LocalDate now = LocalDate.now();
            String datePath = String.format("%d/%02d/%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
            Path directoryPath = Paths.get(uploadPath, category, datePath);
            
            // 确保目录存在
            Files.createDirectories(directoryPath);
            
            // 存储文件
            Path targetPath = directoryPath.resolve(newFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            // 返回文件URL
            String fileUrl = String.format("%s/%s/%s/%s", baseUrl, category, datePath, newFilename);
            log.info("文件存储成功: {}", fileUrl);
            
            return fileUrl;
        } catch (IOException e) {
            log.error("文件存储失败", e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
        }
    }
    
    /**
     * 删除文件
     *
     * @param fileUrl 文件URL
     */
    public void deleteFile(String fileUrl) {
        try {
            if (fileUrl == null || !fileUrl.startsWith(baseUrl)) {
                return;
            }
            
            // 从URL中提取文件路径
            String relativePath = fileUrl.substring(baseUrl.length() + 1);
            Path filePath = Paths.get(uploadPath, relativePath);
            
            Files.deleteIfExists(filePath);
            log.info("文件删除成功: {}", fileUrl);
        } catch (IOException e) {
            log.error("文件删除失败: {}", fileUrl, e);
        }
    }
}

