package com.example.fileDemo.application;

import com.example.fileDemo.domain.File;
import com.example.fileDemo.infrastructor.FileRepository;
import com.example.fileDemo.infrastructor.s3.s3Loader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileValidator validator;
    private final s3Loader s3Loader;
    private final FileRepository fileRepository;

    @Transactional
    public void fileUpload(MultipartFile file, String userId) throws IllegalAccessException, IOException {
        String originalFilename = file.getOriginalFilename();
        String s3Filename = createFilename(originalFilename);
        String extension = StringUtils.getFilenameExtension(originalFilename);

        //유효성 검증
        validator.getFileValidator(extension).validate(file, userId);
        //S3 파일 저장
        String url=s3Loader.save(file, s3Filename);
        // 파일 경로 저장
        fileRepository.save(File.of(userId, originalFilename, s3Filename, url));
    }

    @Transactional(readOnly = true)
    public String fileDownload(String filename) {
        File file = fileRepository.findByOriginalFilename(filename)
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 파일입니다."));

        return file.getUrl();
    }

    private String createFilename(String originalFilename) {
        return UUID.randomUUID().toString();
    }

}
