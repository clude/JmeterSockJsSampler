package com.ouyeel.hippo.JmeterSockjsSampler;

import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * @Description
 * @Author clude
 * @Date 2022/10/11
 * @Version 1.0
 **/
public class SocketJsHelper {
    public static final ThreadLocal<WebSocketStompClient> threadLocalCachedConnection = new ThreadLocal<>();
    public static final ThreadLocal<StompSession> threadLocalCachedSession = new ThreadLocal<>();

    public static final ArrayBlockingQueue<String> messageQueue;

    static {
        messageQueue = new ArrayBlockingQueue<>(100);
    }
}
