package com.navercorp.newbie.todolist.config;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.*;
import java.time.LocalDateTime;

public class FileSafer {

    @Value("${ncloud.apiAccessKey}")
    private String accessKey;

    @Value("${ncloud.apiSecretKey}")
    private String secretKey;


    public static String getHash(String filename, String algorithm) throws Exception {
        MessageDigest md = MessageDigest.getInstance(algorithm);

        // file hashing with DigestInputStream
        DigestInputStream dis = new DigestInputStream(new FileInputStream(new File(filename)), md);

        // empty loop to clear the data
        while(dis.read() != -1);
        dis.close();

        // Get the hash's bytes
        byte[] hashBytes = md.digest();

        // bytes to hex
        StringBuffer sb = new StringBuffer();
        for(byte h : hashBytes) {
            sb.append(String.format("%02x", h));
        }

        return sb.toString();
    }

    public String makeSignature() throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        String space = " ";					// one space
        String newLine = "\n";					// new line
        String method = "GET";					// method
        String url = "/photos/puppy.jpg?query1=&query2";	// url (include query string)
        String timestamp = LocalDateTime.now().toString();			// current timestamp (epoch)

        String message = new StringBuilder()
                .append(method)
                .append(space)
                .append(url)
                .append(newLine)
                .append(timestamp)
                .append(newLine)
                .append(accessKey)
                .toString();

        SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(signingKey);

        byte[] rawHmac = mac.doFinal(message.getBytes("UTF-8"));
        String encodeBase64String = Base64.encodeBase64String(rawHmac);

        return encodeBase64String;
    }

    public boolean HashFilter(String filename) throws Exception {
        String apiDomain = "https://filesafer.apigw.ntruss.com";
        String method = "GET";
        long timestamp = System.currentTimeMillis();

        // step 2, 3: Extract hash vlaue of a identified file then add parameter value including hash value and hash type
        String param = "hashCode=" + getHash(filename, "sha-1") + "&hashType=sha1";
        // The apiURL is the value obtained by appending parameter to the end of URI string provided by Hash Filter.
        String apiURL = "/hashfilter/v1/checkHash?" + param;

        // step 4: Create authentication value
//        String signature = makeSignature(method, apiURL, timestamp);
        String signature = makeSignature();

        // step 5: Request API
        URL url = new URL(apiDomain + apiURL);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod(method);
        con.setRequestProperty("x-ncp-apigw-timestamp", Long.toString(timestamp));
        con.setRequestProperty("x-ncp-iam-access-key", accessKey);
        con.setRequestProperty("x-ncp-apigw-signature-v2", signature);
        con.setRequestProperty("accept", "application/json");

        // step 6: Check the response value and determine whether to block
        int httpResponseCode = con.getResponseCode();
        BufferedReader br = null;
        if (httpResponseCode == 200) {
            br = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String readLine;
            StringBuffer httpResponse = new StringBuffer();

            while((readLine = br.readLine()) != null) {
                httpResponse.append(readLine);
            }

            br.close();
            return true;
        }
        return false;
    }
}
