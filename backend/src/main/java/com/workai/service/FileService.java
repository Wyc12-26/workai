package com.workai.service;

import org.springframework.stereotype.Service;

import java.util.UUID;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class FileService {

    private final String uploadDir;

    public FileService() {
        this.uploadDir = System.getProperty("user.dir") + "/uploads";
    }

    /**
     * 保存上传的文件
     */
    public String saveFile(org.springframework.web.multipart.MultipartFile file) throws IOException {
        // 创建上传目录
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 生成唯一文件名
        String fileId = UUID.randomUUID().toString();
        String fileName = fileId + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);

        // 保存文件
        Files.write(filePath, file.getBytes());

        return fileId;
    }
}
