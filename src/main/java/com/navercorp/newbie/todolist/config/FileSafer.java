package com.navercorp.newbie.todolist.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;

public class FileSafer {

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
    public boolean HashFilter(String filename) throws Exception {
        // String accessKey = "AAAAAAAAAAAAAAAAAAAA";
        String apiDomain = "https://filesafer.apigw.ntruss.com";
        String method = "GET";
        long timestamp = System.currentTimeMillis();

        // step 2, 3: Extract hash vlaue of a identified file then add parameter value including hash value and hash type
        String param = "hashCode=" + getHash(filename, "sha-1") + "&hashType=sha1";
        // The apiURL is the value obtained by appending parameter to the end of URI string provided by Hash Filter.
        String apiURL = "/hashfilter/v1/checkHash?" + param;

//        // step 4: Create authentication value
//        String signature = makeSignature(method, apiURL, timestamp);
//
//        // step 5: Request API
//        URL url = new URL(apiDomain + apiURL);
//        HttpURLConnection con = (HttpURLConnection)url.openConnection();
//        con.setRequestMethod(method);
//        con.setRequestProperty("x-ncp-apigw-timestamp", Long.toString(timestamp));
//        con.setRequestProperty("x-ncp-iam-access-key", accessKey);
//        con.setRequestProperty("x-ncp-apigw-signature-v2", signature);
//        con.setRequestProperty("accept", "application/json");
//
//        // step 6: Check the response value and determine whether to block
//        int httpResponseCode = con.getResponseCode();
//        BufferedReader br = null;
//        if (httpResponseCode == 200) {
//            br = new BufferedReader(new InputStreamReader(con.getInputStream()));
//
//            String readLine;
//            StringBuffer httpResponse = new StringBuffer();
//
//            while((readLine = br.readLine()) != null) {
//                httpResponse.append(readLine);
//            }
//            br.close();
//            return true;
//        }
//        return false;
        return true;
    }
}
