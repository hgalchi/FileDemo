package com.example.fileDemo.infrastructor.s3;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.UploadPartRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
@RequiredArgsConstructor
@Slf4j
public class s3Loader {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${file.temp.directory}")
    private String tempFileDirectory;

    private final AmazonS3Client s3Client;

    public String save(MultipartFile multipartFile, String filename) throws IOException {

        //단일 파일 사이즈 최대 5GB
        long partSize = 5L * 1024 * 1024;
        File file = convertToFile(multipartFile);
        long fileSize = file.length();
        long filePosition = 0;

        List<Future<PartETag>> futures = new ArrayList<>();
        List<PartETag> partETags = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(bucket, filename);
        InitiateMultipartUploadResult initResponse = s3Client.initiateMultipartUpload(initRequest);

        try {
            int partNumber = 1;
            while (filePosition < fileSize) {
                long currentPartSize = Math.min(partSize, (fileSize - filePosition));
                long currentPartPosition = filePosition;

                final int currentPartNumber = partNumber++;

                //각 파트에 대한 객체 생성
                Future<PartETag> future = executor.submit(() -> {
                    UploadPartRequest uploadRequest = new UploadPartRequest()
                            .withBucketName(bucket)
                            .withKey(filename)
                            .withUploadId(initResponse.getUploadId())
                            .withPartNumber(currentPartNumber)
                            .withFileOffset(currentPartPosition)
                            .withFile(file)
                            .withPartSize(currentPartSize);
                    //파트 업로드
                    UploadPartResult uploadResult = s3Client.uploadPart(uploadRequest);
                    log.info(currentPartNumber + "번째 파트 업로드 완료: " + uploadResult.getPartETag());
                    return uploadResult.getPartETag();
                });

                futures.add(future);
                filePosition += partSize;
            }

            for (Future<PartETag> future : futures) {
                partETags.add(future.get());
            }

            //멀티 파트 업로드 완료 요청
            CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(
                    bucket, filename, initResponse.getUploadId(), partETags
            );
            s3Client.completeMultipartUpload(compRequest);

        } catch (Exception e) {
            s3Client.abortMultipartUpload(new AbortMultipartUploadRequest(bucket, filename, initResponse.getUploadId()));
            throw new RuntimeException("업로드 실패", e);
        } finally {
            executor.shutdown();
            file.delete();
        }

       return s3Client.getUrl(bucket, filename).toString();
    }

    private void uploadPart() {

    }

    private File convertToFile(MultipartFile multipartFile) throws IOException {
        File convFile = java.io.File.createTempFile(tempFileDirectory+"\\upload-", ".csv");
        multipartFile.transferTo(convFile);
        return convFile;
    }

}
