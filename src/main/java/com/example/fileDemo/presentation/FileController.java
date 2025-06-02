package com.example.fileDemo.presentation;

import com.example.fileDemo.application.FileService;
import com.example.fileDemo.infrastructor.s3.s3Loader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
@Slf4j
public class FileController {

    private final FileService fileService;
    private final s3Loader loader;

    @PostMapping("/upload")
    public ResponseEntity<String> addFile(@RequestParam("File") MultipartFile file, @RequestHeader("userId") String userId) throws IllegalAccessException, IOException {
        fileService.fileUpload(file, userId);
        return ResponseEntity.ok("suc");
    }

    @GetMapping("/download")
    public ResponseEntity<String> getFile(@RequestHeader("filename") String filename) {
        String url = fileService.fileDownload(filename);
        return ResponseEntity.ok(url);
    }
}
