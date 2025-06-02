package com.example.fileDemo.application;

import com.example.fileDemo.infrastructor.s3Loader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileValidator validator;
    private final s3Loader s3Loader;

    public void FileUpload(MultipartFile file, String userId) throws IllegalAccessException, IOException {
        String originalFilename = file.getOriginalFilename();
        String s3Filename = createFilename(originalFilename);
        String extension = StringUtils.getFilenameExtension(originalFilename);
        //유효성 검증
        validator.getFileValidator(extension).validate(file, userId);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        //S3 파일 저장
        String url=s3Loader.singleUpload(file, s3Filename);
        stopWatch.stop();
        System.out.println("실행 시간 " + stopWatch.getTotalTimeMillis() + "초");
        //TODO : db 파일 경로 저장

    }
    private String createFilename(String originalFilename) {
        return UUID.randomUUID().toString();
    }



}
