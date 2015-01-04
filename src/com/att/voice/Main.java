package com.att.voice;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private AmazonEchoApi amazonEchoApi;
    
    private String username;
    private String password;
    private Map<String, String> authMap;
    private AttDigitalLife dl;
    private TTS tts;
    private RTCWebSocket rtcWebSocket;
    
    public static void main(String[] args) throws IOException, InterruptedException {
        Main main = new Main();
        main.init(args);
        main.setupMorningTask();
        main.listenEcho();
        
        // smart-plug switch on|off
        // garage-door-controller garage-door-control open|close
        // camera capture video|image
        // thermostat thermostat-mode off|heat|cool|auto|save-heat|save-cool|fan-only
        // door-lock lock unlock|lock
    }    
    
    private void init(String[] args) throws UnknownHostException {
        if (null == args || null == args[0] || null == args[1]) {
            return;
        } 
        username = args[0];
        password = args[1];
        
        tts = new TTS();
        //rtcWebSocket = new RTCWebSocket();
        //rtcWebSocket.start();
        amazonEchoApi = new AmazonEchoApi("https://pitangui.amazon.com","USERNAME", "PASSWORD");
        amazonEchoApi.httpLogin();
        
        dl = new AttDigitalLife(username, password);
        authMap = dl.authtokens();
    }
    
    private void setupMorningTask() {
        String message = "Good morning,, " 
                + "Tony" 
                + ",, the temperature this morning is " 
                + temperature() 
                + " farenheit. Do you want me to change the temperature?";
        
        tts.say(message, "good-morning.wav");
    }
    
    private void listenEcho() {
        while (true) {
            String deviceGUID, response;
            String command;
            try {
                command = amazonEchoApi.getLatestTodo();
                if (command != null) {
                    System.out.println(command);
                    
                    if (command.contains("temperature")) {
                        deviceGUID = dl.getDeviceGUID("thermostat", authMap);
                        response = dl.deviceAction(authMap, deviceGUID, "heat-setpoint", "72");
                        System.out.println("thermostat: " + response);
                        
                        deviceGUID = dl.getDeviceGUID("smart-plug", authMap);
                        response = dl.deviceAction(authMap, deviceGUID, "switch", "on");
                        System.out.println("smart-plug: " + response);
                        
                        deviceGUID = dl.getDeviceGUID("door-lock", authMap);
                        response = dl.deviceAction(authMap, deviceGUID, "lock", "unlock");
                        System.out.println("door-lock: " + response);
                        
                        tts.say("I have set the temperature to 72 degrees. I started the coffe for you. "
                                + "By the way,, have you taken your medication today? Also you have one new email from Cindy.", "set-temperature.wav");
                        
                    } else if (command.contains("tired")) {
                        //tts.say("Ok I will lock the doors, turn off the lights, close the garage door, and set the alarm for you");
                        goodnight();
                        
                        //return;
                    } else if (command.contains("email")){
                        tts.say("Cindy wrote: How are you dad! Don't forget to call your grandson,"
                                + " it is his birthday today! Love you!", "read-email.wav");
                    
                    } else if (command.contains("call")){
                        //rtcWebSocket.sendMessage("call", this);
                        tts.say("I'm calling your grandson.", "calling.wav");
                    }
 
                    
                } else{
                    System.out.println("No new commands");
                }
                Thread.sleep(5000);
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }  
    }
    
    
    private String temperature() {
        String s = dl.getAttribute(authMap, dl.getDeviceGUID("thermostat", authMap), "temperature");
        int f = (Integer.valueOf(s)/2 - 40) * (9/5) + 32;
        return Integer.toString(f);
    }
    
    public void goodnight() {
        System.out.println("Testing");
        
        String deviceGUID = dl.getDeviceGUID("smart-plug", authMap);
        String response = dl.deviceAction(authMap, deviceGUID, "switch", "off");
        System.out.println("smart-plug: " + response);
        
        deviceGUID = dl.getDeviceGUID("thermostat", authMap);
        response = dl.deviceAction(authMap, deviceGUID, "heat-setpoint", "72");
        System.out.println("thermostat: " + response);
        
        deviceGUID = dl.getDeviceGUID("door-lock", authMap);
        response = dl.deviceAction(authMap, deviceGUID, "lock", "lock");
        System.out.println("door-lock: " + response);
        
        deviceGUID = dl.getDeviceGUID("garage-door-controller", authMap);
        response = dl.deviceAction(authMap, deviceGUID, "garage-door-control", "close");
        System.out.println("garage: " + response);
        
        String message = "I went ahead and locked the house,"
                + " turn off the lights, close the garage door and set the alarm. Have a good night!";
        
        tts.say(message, "good-night.wav");
    }
}