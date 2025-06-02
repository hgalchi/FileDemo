package com.example.fileDemo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;
    private String userId;
    private String originalFilename;
    private String s3Filename;
    private String url;

    public static File of(String userId, String originalFilename,String s3Filename, String url) {
        return File.builder()
                .userId(userId)
                .originalFilename(originalFilename)
                .s3Filename(s3Filename)
                .url(url)
                .build();
    }

}
