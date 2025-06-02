package com.example.fileDemo.infrastructor;

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


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class s3Loader {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3Client s3Client;

    /*
    단일 업로드 - Stream
     */
    public String singleUpload(MultipartFile file, String filename){
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(file.getSize());
        objectMetadata.setContentType(file.getContentType());

        try (InputStream inputStream = file.getInputStream()) {
            s3Client.putObject(new PutObjectRequest(bucket, filename, inputStream, objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (IOException e) {
            throw new IllegalArgumentException("S3 업로드 실패");
        }
        return s3Client.getUrl(bucket, filename).toString();
    }

    /*
    다중 업로드 - file
     */
    public String multipartFileUpload(MultipartFile multipartFile, String filename) throws IOException {

        //단일 파일 사이즈 최대 5GB
        long partSize = 5 * 1024 * 1024;
        long filePosition = 0;
        List<PartETag> partETags = new ArrayList<>();
        File file = convertToFile(multipartFile);

        try {
            long fileSize = file.length();
            InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(bucket, filename);
            InitiateMultipartUploadResult initResponse = s3Client.initiateMultipartUpload(initRequest);

            for (int i = 1; filePosition < fileSize; i++) {
                //마지막 part가 partSize보다 작은 경우
                partSize = Math.min(partSize, (fileSize - filePosition));

                // 각 프트에 대한 객체 생성
                UploadPartRequest uploadPartRequest = new UploadPartRequest()
                        .withBucketName(bucket)
                        .withKey(filename)
                        .withUploadId(initResponse.getUploadId())
                        .withPartNumber(i)
                        .withFileOffset(filePosition)
                        .withFile(file)
                        .withPartSize(partSize);

                // 각 파트를 업로드
                UploadPartResult uploadResult = s3Client.uploadPart(uploadPartRequest);
                // ETag를 partEtags에 저장
                partETags.add(uploadResult.getPartETag());

                filePosition += partSize;
                log.info(i + "번째:" + uploadResult.getPartETag());
            }
            // 멀티 파트 업로드 완료 요청
            CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(bucket, filename, initResponse.getUploadId(), partETags);
            s3Client.completeMultipartUpload(compRequest);


        } catch (AmazonServiceException e) {
            e.printStackTrace();
        } catch (SdkClientException e) {
            e.printStackTrace();
        }finally {
            file.delete();
        }
        String url = s3Client.getUrl(bucket, filename).toString();
        log.info("file down url:" + url);
        return url;
    }

    /*
    다중 업로드 - stream
     */
    public String uploadMultipartStream(MultipartFile file, String keyName) throws IOException {
        long partSize = 5 * 1024 * 1024; // 최소 5MB
        long uploadedBytes = 0;
        int partNumber = 1;
        List<PartETag> partETags = new ArrayList<>();

        InitiateMultipartUploadRequest initRequest =
                new InitiateMultipartUploadRequest(bucket, keyName);
        InitiateMultipartUploadResult initResponse = s3Client.initiateMultipartUpload(initRequest);

        try (InputStream inputStream = file.getInputStream()) {
            byte[] buffer = new byte[(int) partSize];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                InputStream partStream = new ByteArrayInputStream(buffer, 0, bytesRead);

                UploadPartRequest uploadRequest = new UploadPartRequest()
                        .withBucketName(bucket)
                        .withKey(keyName)
                        .withUploadId(initResponse.getUploadId())
                        .withPartNumber(partNumber++)
                        .withInputStream(partStream)
                        .withPartSize(bytesRead);

                UploadPartResult uploadResult = s3Client.uploadPart(uploadRequest);
                partETags.add(uploadResult.getPartETag());

                uploadedBytes += bytesRead;
            }

            CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(
                    bucket, keyName, initResponse.getUploadId(), partETags);

            s3Client.completeMultipartUpload(compRequest);
        } catch (Exception e) {
            s3Client.abortMultipartUpload(new AbortMultipartUploadRequest(bucket, keyName, initResponse.getUploadId()));
            throw new RuntimeException("S3 multipart upload failed", e);
        }

        return s3Client.getUrl(bucket, keyName).toString();
    }


    private File convertToFile(MultipartFile multipartFile) throws IOException {
        String path = "C:\\\\uploadTest";
        File convFile = File.createTempFile(path+"\\upload-", ".csv");
        multipartFile.transferTo(convFile);
        return convFile;
    }

}
