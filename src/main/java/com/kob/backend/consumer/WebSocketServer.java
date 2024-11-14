package com.kob.backend.consumer;

import com.alibaba.fastjson.JSONObject;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.User;
import com.kob.backend.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@ServerEndpoint("/websocket/{token}")  // 注意不要以'/'结尾
public class WebSocketServer {

    //线程安全的静态变量存储客户端id和websockeserver的对应关系
    final private static ConcurrentHashMap<Integer, WebSocketServer> users = new ConcurrentHashMap<>();
    //匹配池，线程安全
    final private static CopyOnWriteArraySet<User> matchPool = new CopyOnWriteArraySet<>();
    private User user = null;
    private Session session = null;

    private static UserMapper userMapper;
    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        WebSocketServer.userMapper = userMapper;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) throws IOException {
        System.out.println("连接了一个客户端");
        // 建立连接
        int userId = -1;
        try {
            Claims claims = JwtUtil.parseJWT(token);
            userId = Integer.parseInt(claims.getSubject());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if(userId == -1) {
            session.close();
        } else {
            this.session = session;
            this.user = userMapper.selectById(userId);
            users.put(userId, this);
        }
    }

    @OnClose
    public void onClose() {
        System.out.println("断开了一个客户端的连接");
        // 关闭链接
        if(user != null) {
            users.remove(user.getId());
        }
    }

    private void startMatch(){
        System.out.println("调试信息：开始匹配");
        matchPool.add(user);
        while(matchPool.size() >= 2){
            Iterator<User> iterator = matchPool.iterator();
            User a = iterator.next(), b = iterator.next();
            matchPool.remove(a);
            matchPool.remove(b);

            JSONObject respA = new JSONObject();
            JSONObject respB = new JSONObject();
            respA.put("event", "match-found");
            respA.put("opponent_name", b.getUsername());
            respA.put("opponent_photo", b.getPhoto());
            respB.put("event", "match-found");
            respB.put("opponent_name", a.getUsername());
            respB.put("opponent_photo", a.getPhoto());
            users.get(a.getId()).sendMessage(respA.toJSONString());
            users.get(b.getId()).sendMessage(respB.toJSONString());
        }
    }
    private void stopMatch(){
        System.out.println("调试信息：停止匹配");
        matchPool.remove(user);
    }

    @OnMessage
    public void onMessage(String message, Session session) {    //  onMessage，一般用来做分类，根据event的内容，转给不同方法处理
        System.out.println("收到来自客户端的信息");    // 客户端向服务器端发
        JSONObject data = JSONObject.parseObject(message);
        String event = data.getString("event");

        if("start-match".equals(event)) {
            startMatch();
        } else if("stop-match".equals(event)) {
            stopMatch();
        }
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