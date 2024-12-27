package com.kob.backend.consumer.handler;


import com.alibaba.fastjson.JSONObject;

public interface MessageHandler {
    void handle(JSONObject message);
}
