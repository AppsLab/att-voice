/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.att.voice;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

/**
 *
 * @author osvaldo
 */
public class RTCWebSocket extends WebSocketServer {
    private WebSocket socket;
    private Main main;
    
    public RTCWebSocket() throws UnknownHostException {
        super(new InetSocketAddress(8887));
        System.out.println("WebSocket Started");
    }

    @Override
    public void onOpen(WebSocket ws, ClientHandshake ch) {
        System.out.println("onOpen");
        this.socket = ws;
    }

    @Override
    public void onClose(WebSocket ws, int i, String string, boolean bln) {
        System.out.println("onClose");
        main.goodnight();
    }

    @Override
    public void onMessage(WebSocket ws, String string) {
        System.out.println("onMessage");
    }

    @Override
    public void onError(WebSocket ws, Exception excptn) {
        System.out.println("onError");
    }
    
    public void sendMessage(String text, Main main) {
        this.socket.send(text);
        this.main = main;
    }
}
