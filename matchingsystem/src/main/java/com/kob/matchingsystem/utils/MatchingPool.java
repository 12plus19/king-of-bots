package com.kob.matchingsystem.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class MatchingPool extends Thread{
    private static List<Player> players = new ArrayList<>();          //  对这个列表加锁
    private final ReentrantLock lock = new ReentrantLock();
    private static RestTemplate restTemplate;
    private final static String startGameUrl= "http://localhost:3000/pk/start/game/";

    @Autowired
    public void setRestTemplate (RestTemplate restTemplate){
        MatchingPool.restTemplate = restTemplate;
    };

    public void addPlayer(Integer userId, Integer rating, Integer botId){
        lock.lock();
        try {
            players.add(new Player(userId, rating, botId, 0));
        } finally {
            lock.unlock();
        }
    }
    public void removePlayer(Integer userId){
        lock.lock();
        try {
            List<Player> newPlayers = new ArrayList<>();
            for(Player player : players){
                if(!player.getUserId().equals(userId)){
                    newPlayers.add(player);
                }
            }
            players = newPlayers;
        }finally {
            lock.unlock();
        }
    }

    private void increasingWaitingTime(){    //  所有玩家等待时间加一
        lock.lock();
        try {
            for(Player player : players){
                player.setWaitingTime(player.getWaitingTime() + 1);
            }
        }finally {
            lock.unlock();
        }
    }
    private void matchPlayers(){   //  尝试匹配所有玩家
        System.out.println(players);
        lock.lock();
        try {
            boolean[] used = new boolean[players.size()];
            for(int i = 0; i < players.size(); i++){
                if(used[i])continue;
                for(int j = i + 1; j < players.size(); j++){
                    if(used[j])continue;
                    Player a = players.get(i), b = players.get(j);
                    if(checkMatched(a, b)){
                        used[i] = used[j] = true;
                        sendResult(a, b);
                        break;
                    }
                }
            }
            List<Player> newPlayers = new ArrayList<>();
            for(int i = 0; i < players.size(); i++){
                if(!used[i]){
                    newPlayers.add(players.get(i));
                }
            }
            players = newPlayers;
        } finally {
            lock.unlock();
        }
    }
    private boolean checkMatched(Player a, Player b){
        int ratingDelta = Math.abs(a.getRating() - b.getRating());
        int waitingTime = Math.min(a.getWaitingTime(), b.getWaitingTime());
        return waitingTime * 10 >= ratingDelta;
    }
    private void sendResult(Player a, Player b){    //  返回匹配结果
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("aId", a.getUserId().toString());
        data.add("aBotId", a.getBotId().toString());
        data.add("bId", b.getUserId().toString());
        data.add("bBotId", b.getBotId().toString());
        restTemplate.postForObject(startGameUrl, data, String.class);
    }

    @Override
    public void run() {
        while(true){
            try {
                Thread.sleep(1000);
                increasingWaitingTime();
                matchPlayers();
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
