package com.kob.backend.consumer.handler;

import com.alibaba.fastjson.JSONObject;
import com.kob.backend.consumer.WebSocketServer;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class StartMatchHandler implements MessageHandler {
    private WebSocketServer server;
    public StartMatchHandler(WebSocketServer server) {
        this.server = server;
    }
    @Override
    public void handle(JSONObject message) {
        Integer botId = message.getInteger("botId");
        System.out.println("调试信息：开始匹配");
        MultiValueMap<String, String> postData = new LinkedMultiValueMap<>();   
        postData.add("userId", server.getUser().getId().toString());
        postData.add("rating", server.getUser().getRating().toString());
        postData.add("botId", botId.toString());
        String response = server.getRestTemplate().postForObject(server.getAddPlayerUrl(), postData, String.class);
        System.out.println(response);
    }
}
