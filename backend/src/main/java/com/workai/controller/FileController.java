package com.workai.controller;

import com.workai.service.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "文件不能为空"
                ));
            }

            String fileId = fileService.saveFile(file);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "fileId", fileId,
                "message", "文件上传成功"
            ));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "文件上传失败: " + e.getMessage()
            ));
        }
    }
}
