package com.navercorp.newbie.todolist.config;

import com.github.tsohr.JSONArray;
import com.github.tsohr.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.DigestInputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Configuration
public class FileSafer {

    @Value("${ncloud.apiAccessKey}")
    private String accessKey;

    @Value("${ncloud.apiSecretKey}")
    private String secretKey;

    @Value("${ncloud.apiKey}")
    private String apiKey;

    public String getHash(byte[] file, String algorithm) throws Exception {
        MessageDigest md = MessageDigest.getInstance(algorithm);

        // file hashing with DigestInputStream
        DigestInputStream dis = new DigestInputStream(new ByteArrayInputStream(file), md);
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

    public String makeSignature(String method, String apiURL, String timestamp) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException {
        String space = " ";					    // one space
        String newLine = "\n";					// new line

        String message = new StringBuilder()
                .append(method)
                .append(space)
                .append(apiURL) // apiURL "/photos/puppy.jpg?query1=&query2"
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

    public boolean HashFilter(byte[] file, String filename) throws Exception {
        String apiDomain = "https://filesafer.apigw.ntruss.com";
        String method = "GET";
        long timestamp = System.currentTimeMillis();

        // step 2, 3: Extract hash vlaue of a identified file then add parameter value including hash value and hash type
        String param = "hashCode=" + getHash(file, "sha-1") + "&hashType=sha1";
        // The apiURL is the value obtained by appending parameter to the end of URI string provided by Hash Filter.
        String apiURL = "/hashfilter/v1/checkHash?" + param;

        // step 4: Create authentication value
        String signature = makeSignature(method, apiURL, Long.toString(timestamp));

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

            String msg = httpResponse.toString();
            JSONObject jsonObject = new JSONObject(msg);
            JSONArray obj = (JSONArray)jsonObject.get("hashCheckResultList");
            if(obj.isEmpty())
                return true;
        }
        return false;
    }

    public boolean inputFile(byte[] file, String filename, String password) throws Exception {
        String apiDomain = "https://fin-filesafer.apigw.fin-ntruss.com";
        String apiURL = "/filefilter/v1/inputFile";
        String method = "POST";
        String boundary = "----AAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        String crlf = "\r\n";
        String twoHyphens = "--";
        long timestamp = System.currentTimeMillis();

        // Create Authentication Value
        String signature = makeSignature(method, apiURL, Long.toString(timestamp));

        // Request API
        URL url = new URL(apiDomain + apiURL);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setDoOutput(true);
        con.setRequestMethod(method);
        con.setRequestProperty("x-ncp-apigw-timestamp", Long.toString(timestamp));
        con.setRequestProperty("x-ncp-iam-access-key", accessKey);
        con.setRequestProperty("x-ncp-apigw-signature-v2", signature);
        con.setRequestProperty("accept", "application/json");
        con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try {
            // Create request with the identified file added
            DataOutputStream dos = new DataOutputStream(con.getOutputStream());
            // start of request body
            dos.writeBytes(twoHyphens + boundary + crlf);
            dos.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"" + crlf);
            dos.writeBytes("Content-Type: "+ URLConnection.guessContentTypeFromName(filename) + crlf);
            dos.writeBytes(crlf);

            InputStream inputStream = new ByteArrayInputStream(file);
            byte[] buffer = new byte[4096];
            int bytesRead = -1;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead-1);
            }
            dos.writeBytes(crlf);
            inputStream.close();

            // If the file you want to transfer is a zip file with a password, add the following parameters to the request
            if(password != null && !password.isEmpty()) {
                dos.writeBytes(twoHyphens + boundary + crlf);
                dos.writeBytes("Content-Disposition: form-data; name=\"archivePassword\"" + crlf);
                dos.writeBytes(crlf);
                dos.writeBytes(password + crlf);
            }

            // end of request body
            dos.writeBytes(crlf);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + crlf);
            dos.flush();

            // Check the response value
            int responseCode = con.getResponseCode();
            BufferedReader br = null;
            if(responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
                br.close();
                String msg = response.toString();
                JSONObject jsonObject = new JSONObject(msg);
                String returnCode = (String) jsonObject.get("returnCode");
                String returnMessage = (String) jsonObject.get("returnMessage");
                if(returnCode.equals("0") && returnMessage.equals("success")) // check to transfer succeeded
                    return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean getInputFileLog(byte[] file) throws Exception {
        String apiDomain = "https://filesafer.apigw.ntruss.com";
        String method = "POST";
        String apiURL = "/filefilter/v1/getInputFileLog";
        String hash = getHash(file, "sha-1");
        long timestamp = System.currentTimeMillis();

        // step 2: Create Authentication Value
        String signature = makeSignature(method, apiURL, Long.toString(timestamp));

        // step 4: Request API
        URL url = new URL(apiDomain + apiURL);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setDoOutput(true);
        con.setRequestMethod(method);
        con.setRequestProperty("x-ncp-apigw-timestamp", Long.toString(timestamp));
        con.setRequestProperty("x-ncp-iam-access-key", accessKey);
        con.setRequestProperty("x-ncp-apigw-signature-v2", signature);
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("accept", "application/json");

        // step3: Add required parameters with json format
        DataOutputStream dos = new DataOutputStream(con.getOutputStream());

        dos.writeBytes("{\r\n");
        dos.writeBytes("    \"hash\": \"" + hash + "\",\r\n");
        dos.writeBytes("    \"hashType\": \"sha1\"\r\n");
        dos.writeBytes("}");

        // step5: Check the response value and determine whether to block
        int responseCode = con.getResponseCode();
        BufferedReader br = null;
        if(responseCode == 200) {
            br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            String msg = response.toString();
            JSONObject jsonObject = new JSONObject(msg);
            JSONArray obj = (JSONArray)jsonObject.get("inputFileLogList");
            if(obj.isEmpty())
                return true;
        }
        return false;
    }

    public boolean FileFilter(byte[] file, String filename, String password) throws Exception {
        if(inputFile(file, filename, password) == true && getInputFileLog(file) == true){
            return true;
        }
        return false;
    }

    public boolean FileCheck(byte[] file, String filename, String password) throws Exception {
        if(HashFilter(file, filename) == true && FileFilter(file, filename, null) == true)
            return true;
        else
            return false;
    }
}
