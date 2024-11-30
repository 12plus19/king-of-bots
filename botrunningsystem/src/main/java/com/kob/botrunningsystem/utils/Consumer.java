package com.kob.botrunningsystem.utils;

import org.joor.Reflect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class Consumer extends Thread{
    private Bot bot;
    private static RestTemplate restTemplate;
    private final static String receiveBotMoveUrl = "http://127.0.0.1:3000/pk/receive/bot/move/";

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        Consumer.restTemplate = restTemplate;
    }

    public void startTimeout(long timeout, Bot bot){
        this.bot = bot;
        this.start();

        try {
            this.join(timeout);    //  最多等待多长时间，之后执行下面代码
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            this.interrupt();
        }
    }

    private String addUid(String code, String uid){            //   在代码里的类名后面加上uid
        int k = code.indexOf(" implements com.kob.botrunningsystem.utils.BotInterface");
        return code.substring(0, k) + uid + code.substring(k);
    }
    //同一个类名只编译一次，因此在类名后面加一个随机字符串
    @Override
    public void run(){
        UUID uuid = UUID.randomUUID();
        String uid = uuid.toString().substring(0,8);

        BotInterface botInterface = Reflect.compile(
                "com.kob.botrunningsystem.utils.BotTest" + uid,
                addUid(bot.getBotCode(), uid)
        ).create().get();//  创建一个类并获取到
        Integer direction = botInterface.nextMove(bot.getInput());
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("userId", bot.getUserId().toString());
        data.add("direction", direction.toString());
        String resp = restTemplate.postForObject(receiveBotMoveUrl, data, String.class);
        System.out.println(resp);
        System.out.println(direction);
    }
}
