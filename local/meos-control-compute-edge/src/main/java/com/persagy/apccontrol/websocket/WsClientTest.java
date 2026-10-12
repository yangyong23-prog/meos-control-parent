package com.persagy.apccontrol.websocket;

import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author shaohongbo
 * @date 2023年03月28日 21:07
 */
@Slf4j
public class WsClientTest {
    private WebSocketClient webSocketClient = null;

    public WsClientTest() {
        String url = "ws://127.0.0.1:8849/webSocketServer/Pj6101122010";
        WebSocketClient webSocketClient = null;
        try {
            webSocketClient = new WebSocketClient(new URI(url)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {

                }

                @Override
                public void onMessage(String message) {
                    if (!message.equals("pong") && !message.contains("服务端心跳")) {
                        log.info(message);
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {

                }

                @Override
                public void onError(Exception ex) {

                }
            };
        } catch (URISyntaxException e) {
            System.err.println("Failed to parse URI: " + url);
            // 可选：抛出异常或返回错误状态
            throw new RuntimeException("Invalid URI syntax", e);

        }
        webSocketClient.connect();
        this.webSocketClient = webSocketClient;
    }

    public static void main(String[] args) {

        final WsClientTest wsClientTest = new WsClientTest();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(Long.parseLong("1000"));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    wsClientTest.send();
                }

            }
        }).start();


    }

    public synchronized void send() {
        this.webSocketClient.send("ping");
    }

}
