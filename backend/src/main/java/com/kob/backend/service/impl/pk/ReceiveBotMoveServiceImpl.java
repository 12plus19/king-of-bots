package com.kob.backend.service.impl.pk;

import com.kob.backend.consumer.Game;
import com.kob.backend.consumer.WebSocketServer;
import com.kob.backend.service.pk.ReceiveBotMoveService;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ReceiveBotMoveServiceImpl implements ReceiveBotMoveService {

    @Override
    public String receiveBotMove(Integer userId, Integer direction) {
        System.out.println("receive bot move success");
        if(WebSocketServer.users.get(userId) != null){
            Game game = WebSocketServer.users.get(userId).game;
            if(game != null){
                if(Objects.equals(game.getPlayerA().getId(), userId)){
                    game.setNextStepA(direction);
                } else if(Objects.equals(game.getPlayerB().getId(), userId)){
                    game.setNextStepB(direction);
                }
            }
        }
        return "receive bot move success";
    }
}
