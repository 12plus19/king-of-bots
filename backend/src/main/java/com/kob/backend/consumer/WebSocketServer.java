package com.kob.backend.consumer;

import com.alibaba.fastjson.JSONObject;
import com.kob.backend.mapper.BotMapper;
import com.kob.backend.mapper.RecordMapper;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.Bot;
import com.kob.backend.pojo.User;
import com.kob.backend.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

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
    //final private static CopyOnWriteArraySet<User> matchPool = new CopyOnWriteArraySet<>();

    private User user = null;
    private Session session = null;
    public Game game = null;
    private final String addPlayerUrl = "http://localhost:3001/player/add/";
    private final String removePlayerUrl = "http://localhost:3001/player/remove/";

    public static UserMapper userMapper;
    public static RecordMapper recordMapper;
    public static RestTemplate restTemplate;
    private static BotMapper botMapper;
    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        WebSocketServer.userMapper = userMapper;
    }
    @Autowired
    public void setRecordMapper(RecordMapper recordMapper) {
        WebSocketServer.recordMapper = recordMapper;
    }
    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        WebSocketServer.restTemplate = restTemplate;
    }
    @Autowired
    public void setBotMapper(BotMapper botMapper) {
        WebSocketServer.botMapper = botMapper;
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
            //matchPool.remove(user);
        }
    }
    @OnMessage
    public void onMessage(String message, Session session) {    //  onMessage，一般用来做分类，根据event的内容，转给不同方法处理
        System.out.println("收到来自客户端的信息");    // 客户端向服务器端发
        JSONObject data = JSONObject.parseObject(message);
        String event = data.getString("event");

        if("start-match".equals(event)) {
            startMatch(data.getInteger("bot_id"));
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

    //在机器人对战时，人的输入不接收
    private void move(int d) {
        if(game.getPlayerA().getId().equals(user.getId())) {
            if(game.getPlayerA().getBotId() == -1)game.setNextStepA(d);
        } else if(game.getPlayerB().getId().equals(user.getId())) {
            if(game.getPlayerB().getBotId() == -1)game.setNextStepB(d);
        }
    }
    public static void startGame(Integer aId, Integer aBotId, Integer bId, Integer bBotId) {
        User a = userMapper.selectById(aId);User b = userMapper.selectById(bId);
        Bot aBot = botMapper.selectById(aBotId);Bot bBot = botMapper.selectById(bBotId);

        Game game = new Game(ROWS, COLS, INNER_WALLS_COUNT, a.getId(), aBot, b.getId(), bBot);
        game.createGameMap();
        if(users.get(a.getId()) != null){     //在玩家匹配时意外断开，但是玩家仍在匹配池中的情况，此时WebSocketServer是空，会空指针
            users.get(a.getId()).game = game;
        }
        if(users.get(b.getId()) != null){
            users.get(b.getId()).game = game;
        }
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

        if(users.get(a.getId()) != null){
            users.get(a.getId()).sendMessage(respA.toJSONString());
        }
        if(users.get(b.getId()) != null){
            users.get(b.getId()).sendMessage(respB.toJSONString());
        }

    }
    //先点的左下角后点的右上角
    private void startMatch(Integer botId){
        System.out.println("调试信息：开始匹配");
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("userId", user.getId().toString());
        data.add("rating", user.getRating().toString());
        data.add("botId", botId.toString());
        String resp = restTemplate.postForObject(addPlayerUrl, data, String.class);
        System.out.println(resp);
//        matchPool.add(user);
//        while(matchPool.size() >= 2){
//            Iterator<User> iterator = matchPool.iterator();     //  先进去的是a，左下
//            User a = iterator.next(), b = iterator.next();
//            matchPool.remove(a);
//            matchPool.remove(b);
//
//
//        }
    }
    private void stopMatch(){
        System.out.println("调试信息：停止匹配");
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("userId", user.getId().toString());
        restTemplate.postForObject(removePlayerUrl, data, String.class);
        //matchPool.remove(user);
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