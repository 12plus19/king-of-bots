package com.kob.backend.consumer;

import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/websocket/{token}")  // 注意不要以'/'结尾
public class WebSocketServer {

    //线程安全的静态变量存储客户端id和websockeserver的对应关系
    private static ConcurrentHashMap<Integer, WebSocketServer> users = new ConcurrentHashMap<>();
    private User user = null;
    private Session session = null;

    private static UserMapper userMapper;
    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        System.out.println("连接了一个客户端");
        // 建立连接
        this.session = session;
        int userId = Integer.parseInt(token);
        User user = userMapper.selectById(userId);
        this.user = user;
        users.put(userId, this);
    }

    @OnClose
    public void onClose() {
        System.out.println("断开了一个客户端的连接");
        // 关闭链接
        if(user != null) {
            users.remove(user.getId());
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("收到来自客户端的信息");
        // 客户端向服务器端发
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    public void sendMessage(String message) {     //  服务器端向客户端发送
        synchronized (session) {
            try{
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}