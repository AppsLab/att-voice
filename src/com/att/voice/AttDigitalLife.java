/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.att.voice;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 *
 * @author nportuga
 */
public class AttDigitalLife {
    private String username;
    private String password;
    private final HttpClient httpclient = HttpClientBuilder.create().build();
    
    private final String HTTP_PROTOCOL = "http";
    private final String APP_KEY = "XXXXXXXX";
    private final String DIGITAL_LIFE_PATH = "systest.digitallife.att.com";
    
    private final String USER_ID_PARAMETER = "userId";
    private final String PASSWORD_PARAMETER = "password";
    private final String DEVICE_PARAMETER = "device";
    private final String DOMAIN_PARAMETER = "domain";
    private final String APP_KEY_PARAMETER = "appKey";
    private final String ID = "id";
    
    public AttDigitalLife(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    // http://systest.digitallife.att.com/penguin/api/authtokens?userId=553474463&password=NO-PASSWD&domain=DL&appKey=NE_D2BAB666CCE9A74E_1
    
    public Map<String, String> authtokens() {
        Map<String, String> authMap = new HashMap<>();
        String json = "";
        try {
            URIBuilder builder = new URIBuilder();
            builder.setScheme(HTTP_PROTOCOL).setHost(DIGITAL_LIFE_PATH)
                    .setPath("/penguin/api/authtokens")
                    .setParameter(USER_ID_PARAMETER, username)
                    .setParameter(PASSWORD_PARAMETER, password)
                    .setParameter(DOMAIN_PARAMETER, "DL")
                    .setParameter(APP_KEY_PARAMETER, APP_KEY);
            
            URI uri = builder.build();
            HttpPost httpPost = new HttpPost(uri);
            HttpResponse httpResponse = httpclient.execute(httpPost);

            httpResponse.getEntity();
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                json = EntityUtils.toString(entity);
            }
            
            JSONObject jsonObject = new JSONObject(json);
            JSONObject content = jsonObject.getJSONObject("content");
            authMap.put("id", content.getJSONArray("gateways").getJSONObject(0).getString("id"));
            authMap.put("Authtoken", content.getString("authToken"));
            authMap.put("Requesttoken", content.getString("requestToken"));
            
            authMap.put("Appkey", APP_KEY);
            
            if (content.has("contact") 
                    && content.getJSONObject("contact").has("firstName") 
                    && content.getJSONObject("contact").has("lastName")) {
                authMap.put("name", content.getJSONObject("contact").getString("firstName") 
                    + " " 
                    + content.getJSONObject("contact").getString("lastName"));
            }

            return authMap;
        } catch (IOException | URISyntaxException ex) {
            Logger.getLogger(AttDigitalLife.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    } 
    
    public String getDeviceGUID(String device, Map<String, String> authMap) {
        try {
            URIBuilder builder = new URIBuilder();
            builder.setScheme("https").setHost(DIGITAL_LIFE_PATH)
                    .setPath("/penguin/api/" + authMap.get("id") + "/devices");
            
            URI uri = builder.build();
            HttpGet httpget = new HttpGet(uri);
            httpget.setHeader("Authtoken", authMap.get("Authtoken"));
            httpget.setHeader("Requesttoken", authMap.get("Requesttoken"));
            httpget.setHeader("Appkey", authMap.get("Appkey"));

            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
                @Override
                public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }
            };
            String responseBody = httpclient.execute(httpget, responseHandler);
            
            String json = responseBody.trim();
            JSONObject jsonObject = new JSONObject(json);
            
            JSONArray array = jsonObject.getJSONArray("content");
            
            for (int i = 0; i <= array.length(); i++) {
                JSONObject d = array.getJSONObject(i);
                String type = d.getString("deviceType");
                if (type.equalsIgnoreCase(device)) {
                    return d.getString("deviceGuid");
                }
            }
        }
        catch(URISyntaxException | IOException | JSONException ex){
            System.err.println(ex.getMessage());
            return null;
        }
        return null;
    }
    
    public String deviceAction(Map<String, String> authMap, String deviceGUID, String action, String value) {
        String json = null;
        try {
            URIBuilder builder = new URIBuilder();
            builder.setScheme(HTTP_PROTOCOL).setHost(DIGITAL_LIFE_PATH)
                    .setPath("/penguin/api/" + authMap.get("id") + "/devices/" + deviceGUID + "/" + action + "/" + value);
            
            URI uri = builder.build();
            HttpPost httpPost = new HttpPost(uri);
            httpPost.setHeader("Authtoken", authMap.get("Authtoken"));
            httpPost.setHeader("Requesttoken", authMap.get("Requesttoken"));
            httpPost.setHeader("Appkey", authMap.get("Appkey"));
            HttpResponse httpResponse = httpclient.execute(httpPost);

            httpResponse.getEntity();
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                json = EntityUtils.toString(entity);
            }
            
            return new JSONObject(json).getString("status");
        } catch (IOException | URISyntaxException ex) {
            Logger.getLogger(AttDigitalLife.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    public String getAttribute(Map<String, String> authMap, String deviceGUID, String attribute) {
        try {
            URIBuilder builder = new URIBuilder();
            builder.setScheme("https").setHost(DIGITAL_LIFE_PATH)
                    .setPath("/penguin/api/" + authMap.get("id") + "/devices/" + deviceGUID + "/" + attribute);
            
            URI uri = builder.build();
            HttpGet httpget = new HttpGet(uri);
            httpget.setHeader("Authtoken", authMap.get("Authtoken"));
            httpget.setHeader("Requesttoken", authMap.get("Requesttoken"));
            httpget.setHeader("Appkey", authMap.get("Appkey"));

            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
                @Override
                public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }
            };
            String responseBody = httpclient.execute(httpget, responseHandler);
            
            String json = responseBody.trim();
            JSONObject content = new JSONObject(json);
            return content.getJSONObject("content").getString("value");
        }
        catch(URISyntaxException | IOException | JSONException ex){
            System.err.println(ex.getMessage());
            return null;
        }
    }
   
}
