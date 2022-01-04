package com.navercorp.newbie.todolist.config;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

public class ObjectStorage {

    private final String endPoint = "https://kr.object.ncloudstorage.com";
    private final String regionName = "kr-standard";

    @Value("${ncloud.apiAccessKey}")
    private String accessKey;

    @Value("${ncloud.apiSecretKey}")
    private String secretKey;

    @Value("${objectstorage.bucketName}")
    private String bucketName;

    public void fileUpload(MultipartFile file, String storeFileName) {
        final AmazonS3 s3 = getS3Client();

        try {
            ObjectMetadata objectMetadata = initObjectMetadata(file);
            InputStream inputStream = file.getInputStream();

            s3.putObject(bucketName, storeFileName, inputStream, objectMetadata);

            inputStream.close();
        } catch (SdkClientException | IOException e) {
            e.printStackTrace();
        }
    }

    public ObjectData fileDownload(String storeFileName) {
        final AmazonS3 s3 = getS3Client();

        ObjectData objectData = null;
        try {
            S3Object s3Object = s3.getObject(bucketName, storeFileName);

            objectData = readDataAndContentType(s3Object);
        } catch (SdkClientException | IOException e) {
            e.printStackTrace();
        }

        return objectData;
    }

    private AmazonS3 getS3Client() {
        return AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endPoint, regionName))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .build();
    }

    private ObjectMetadata initObjectMetadata(MultipartFile file) {
        ObjectMetadata objectMetadata = new ObjectMetadata();

        objectMetadata.setContentType(file.getContentType());
        objectMetadata.setContentLength(file.getSize());

        return objectMetadata;
    }

    private ObjectData readDataAndContentType(S3Object s3Object) throws IOException {
        byte[] bytes = null;
        String contentType = null;

        contentType = s3Object.getObjectMetadata().getContentType();
        S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        byte[] bytesArray = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = s3ObjectInputStream.read(bytesArray)) != -1) {
            byteArrayOutputStream.write(bytesArray, 0, bytesRead);
        }
        bytes = byteArrayOutputStream.toByteArray();

        byteArrayOutputStream.close();
        s3ObjectInputStream.close();

        return new ObjectData(bytes, contentType);
    }
}
