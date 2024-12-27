package com.kob.backend.consumer.handler;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.alibaba.fastjson.JSONObject;
import com.kob.backend.consumer.WebSocketServer;

public class StopMatchHandler implements MessageHandler{
    private WebSocketServer server;
    public StopMatchHandler(WebSocketServer server) {
        this.server = server;
    }

    @Override
    public void handle(JSONObject message) {
        System.out.println("调试信息：停止匹配");
        MultiValueMap<String, String> postData = new LinkedMultiValueMap<>();
        postData.add("userId", server.getUser().getId().toString());
        server.getRestTemplate().postForObject(server.getRemovePlayerUrl(), postData, String.class);
    }
}
