/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.att.voice;

/**
 *
 * @author osvaldo
 */
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public final class TTS {
// This will eventually expire, so be sure to pass a valid token when constructing!

    private static String mAuthToken = "OO9iUxudH701x31Zw3u89OqpKynpTjxW";
    private static final String tempFile = "att_tts.wav";
    private CloseableHttpClient httpclient = HttpClients.createDefault();

    // Pass a valid token when constructing or the hard-coded (EXPIRED?!) token will be used.
    TTS(final String authToken) {
        this.mAuthToken = authToken;
        // ensure raspi volume is maximum
        // executeOnCommandLine("amixer set PCM -- $[$(amixer get PCM|grep -o [0-9]*%|sed 's/%//')+100]%");
    }

    TTS() {
        this(mAuthToken);
    }
    

    public void say(String text, String file) {
        
        text = text.replace("\"", "");

        try {

            HttpPost httpPost = new HttpPost("https://api.att.com/speech/v3/textToSpeech");
            httpPost.setHeader("Authorization", "Bearer " + mAuthToken);
            httpPost.setHeader("Accept", "audio/x-wav");
            httpPost.setHeader("Content-Type", "text/plain");
            httpPost.setHeader("Tempo", "-16");
            HttpEntity entity = new StringEntity(text, "UTF-8");

            httpPost.setEntity(entity);
            HttpResponse response = httpclient.execute(httpPost);
            //String result = EntityUtils.toString(response.getEntity());
            HttpEntity result = response.getEntity();

            BufferedInputStream bis = new BufferedInputStream(result.getContent());
            String filePath = System.getProperty("user.dir") + tempFile;
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(filePath)));
            int inByte;
            while ((inByte = bis.read()) != -1) {
                bos.write(inByte);
            }
            bis.close();
            bos.close();

            executeOnCommandLine("afplay " + System.getProperty("user.dir") + "/" + tempFile);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());

        }
    }

    private static void executeOnCommandLine(final String command) {
        try {
            System.out.println("%%% " + command + " %%%");

            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
        } catch (Exception e) {
            System.err.println("ERROR executing command " + command + " " + e);
        }
    }

}
