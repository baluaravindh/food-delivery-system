package com.balu.food_delivery_system.service;

import com.balu.food_delivery_system.config.AwsConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3ClientService {

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    private final S3Client s3Client;

    public String uploadFile(MultipartFile file, String folder) throws IOException {

        //   Step 1: Validate file is not null/empty
        //           throw exception if file is missing or empty
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        //   Step 2: Validate file type (optional - allow only jpg, jpeg, png)
        //           throw exception if invalid type
        String contentType = file.getContentType();
        if (!List.of("image/jpeg", "image/png", "image/jpg").contains(contentType)) {
            throw new RuntimeException("File type must be image/jpeg, image/png, or image/jpg");
        }

        //   Step 3: Generate a unique file name (S3 key)
        //           combine folder + UUID + original filename
        //           e.g., folder + "/" + UUID.randomUUID() + "_" + originalFilename
        String key = folder + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        //   Step 4: Build PutObjectRequest
        //           set bucket, key (from Step 3), content type (file.getContentType())
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();

        //   Step 5: Upload to S3
        //           call s3Client.putObject(request, RequestBody.fromInputStream(...))
        //           note: RequestBody.fromInputStream needs the input stream AND file size
        s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        //   Step 6: Build and return the public URL
        //           format: https://{bucket}.s3.{region}.amazonaws.com/{key}

//        String awsPublicUrl = "https://{bucketName}.s3.{region}.amazonaws.com/{key}";
//        String awsPublicUrl = "https://" + bucketName + ".s3" + region + ".amazonaws.com/" + key;
        String awsPublicUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);

        return awsPublicUrl;
    }

    public void deleteFile(String fileUrl) {

        //   Step 1: Validate fileUrl is not null/empty
        //           throw exception if missing
        if (fileUrl == null || fileUrl.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        //   Step 2: Extract the S3 key from the fileUrl
        //           the URL looks like: https://{bucket}.s3.{region}.amazonaws.com/{key}
        //           you need just the {key} part (everything after ".com/")
        int index = fileUrl.indexOf(".com/");
        String key = fileUrl.substring(index + 5);

        //   Step 3: Build DeleteObjectRequest
        //           set bucket, key (from Step 2)
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        //   Step 4: Call s3Client.deleteObject(request)
        s3Client.deleteObject(deleteRequest);
    }
}
