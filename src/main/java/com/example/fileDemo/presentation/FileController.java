package com.example.fileDemo.presentation;

import com.example.fileDemo.application.FileService;
import com.example.fileDemo.infrastructor.s3Loader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;
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
    @PostMapping("/single")
    public void addFile(@RequestParam("file") MultipartFile file, @RequestHeader("userId") String userId) throws IllegalAccessException, IOException {
        fileService.FileUpload(file,userId);
    }

    @PostMapping("/multipart/file")
    public void addFileWithMultipartFile(@RequestParam("file") MultipartFile file, @RequestHeader(name = "userId", required = false) String userId) throws IllegalAccessException, IOException {
        log.info("File");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        loader.multipartFileUpload(file, userId);
        stopWatch.stop();
        log.info("실행 시간 " + stopWatch.getTotalTimeMillis() + "초");
    }


    @PostMapping("/multipart/stream")
    public void addFileWithMultipartUploadStream(@RequestParam("file") MultipartFile file, @RequestHeader("userId") String userId) throws IllegalAccessException, IOException {
        log.info("Stream");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        loader.uploadMultipartStream(file, userId);
        stopWatch.stop();
        log.info("실행 시간 " + stopWatch.getTotalTimeMillis() + "초");

    }
}
