package com.example.fileDemo.presentation;

import com.example.fileDemo.application.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public void addFile(@RequestParam("file") MultipartFile file, @RequestHeader("userId") String userId) throws IllegalAccessException, IOException {
        fileService.FileUpload(file,userId);
    }

    @GetMapping("/download")
    public void getFile(){

    }
}
