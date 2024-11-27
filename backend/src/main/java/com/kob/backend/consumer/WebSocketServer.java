package com.kob.backend.consumer;

import com.alibaba.fastjson.JSONObject;
import com.kob.backend.mapper.RecordMapper;
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

import static com.kob.backend.constants.Constants.*;

@Component
@ServerEndpoint("/websocket/{token}")  // 注意不要以'/'结尾
public class WebSocketServer {

    //线程安全的静态变量存储客户端id和websockeserver的对应关系
    final public static ConcurrentHashMap<Integer, WebSocketServer> users = new ConcurrentHashMap<>();
    //匹配池，线程安全
    final private static CopyOnWriteArraySet<User> matchPool = new CopyOnWriteArraySet<>();

    private User user = null;
    private Session session = null;
    private Game game = null;


    private static UserMapper userMapper;
    public static RecordMapper recordMapper;
    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        WebSocketServer.userMapper = userMapper;
    }
    @Autowired
    public void setRecordMapper(RecordMapper recordMapper) {
        WebSocketServer.recordMapper = recordMapper;
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
            matchPool.remove(user);
        }
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
        } else if("move".equals(event)) {
            move(data.getInteger("d"));
        }
    }
    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }


    private void move(int d) {
        if(game.getPlayerA().getId().equals(user.getId())) {
            game.setNextStepA(d);
        } else if(game.getPlayerB().getId().equals(user.getId())) {
            game.setNextStepB(d);
        }
    }
    private void startMatch(){
        System.out.println("调试信息：开始匹配");
        matchPool.add(user);
        while(matchPool.size() >= 2){
            Iterator<User> iterator = matchPool.iterator();     //  先进去的是a，左下
            User a = iterator.next(), b = iterator.next();
            matchPool.remove(a);
            matchPool.remove(b);

            Game game = new Game(ROWS, COLS, INNER_WALLS_COUNT, a.getId(), b.getId());
            game.createGameMap();
            users.get(a.getId()).game = game;
            users.get(b.getId()).game = game;
            game.start();

            JSONObject resp = new JSONObject();

            resp.put("a_id", game.getPlayerA().getId());
            resp.put("a_sx", game.getPlayerA().getSx());
            resp.put("a_sy", game.getPlayerA().getSy());

            resp.put("b_id", game.getPlayerB().getId());
            resp.put("b_sx", game.getPlayerB().getSx());
            resp.put("b_sy", game.getPlayerB().getSy());

            resp.put("map", game.getGameMap());

            JSONObject respA = new JSONObject();
            JSONObject respB = new JSONObject();
            respA.put("opponent_name", b.getUsername());
            respA.put("opponent_photo", b.getPhoto());
            respA.put("event", "match-found");
            respA.put("me", "A");
            respA.put("game", resp);

            respB.put("opponent_name", a.getUsername());
            respB.put("opponent_photo", a.getPhoto());
            respB.put("event", "match-found");
            respB.put("me", "B");
            respB.put("game", resp);

            users.get(a.getId()).sendMessage(respA.toJSONString());
            users.get(b.getId()).sendMessage(respB.toJSONString());
        }
    }
    private void stopMatch(){
        System.out.println("调试信息：停止匹配");
        matchPool.remove(user);
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