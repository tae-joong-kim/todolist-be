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

    public ObjectMetadata initObjectMetadata(MultipartFile multipartFile) {
        ObjectMetadata objectMetadata = new ObjectMetadata();

        objectMetadata.setContentType(multipartFile.getContentType());
        objectMetadata.setContentLength(multipartFile.getSize());

        return objectMetadata;
    }

    public void fileUpload(MultipartFile multipartFile, String storeFileName) {
        // S3 client
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endPoint, regionName))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .build();

        // upload file
        try {
            ObjectMetadata objectMetadata = initObjectMetadata(multipartFile);
            s3.putObject(new PutObjectRequest(bucketName, storeFileName, multipartFile.getInputStream(), objectMetadata));
        } catch (SdkClientException | IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] fileDownload(String storeFileName) {
        // S3 client
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endPoint, regionName))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .build();

        byte[] bytes = null;

        // download object
        try {
            S3Object s3Object = s3.getObject(bucketName, storeFileName);
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
        } catch (SdkClientException | IOException e) {
            e.printStackTrace();
        }

        return bytes;
    }
}
