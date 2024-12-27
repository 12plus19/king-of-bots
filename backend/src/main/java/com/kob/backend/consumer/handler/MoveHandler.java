package com.kob.backend.consumer.handler;


import com.alibaba.fastjson.JSONObject;
import com.kob.backend.consumer.WebSocketServer;



public class MoveHandler implements MessageHandler{
    private WebSocketServer server;
    private DirectionAdapter directionAdapter;
    public MoveHandler(WebSocketServer server, DirectionAdapter directionAdapter) {
        this.server = server;
        this.directionAdapter = directionAdapter;
    }

    @Override
    public void handle(JSONObject message) {
        String directionInput = message.getString("d");
        Integer direction = directionAdapter.convertDirection(directionInput);
        if(direction != null){
            server.move(direction);
        }
    }

}
