package com.kob.backend.consumer;

import com.alibaba.fastjson.JSONObject;
import com.kob.backend.pojo.Bot;
import com.kob.backend.pojo.Record;
import com.kob.backend.pojo.User;
import lombok.Getter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class Game extends Thread{
    private final Integer rows;
    private final Integer cols;
    private final Integer innerWallsCount;
    @Getter
    private final int[][] gameMap;
    private final static int[] dx = {-1, 0, 1, 0};
    private final static int[] dy = {0, 1, 0, -1};
    @Getter
    private final Player playerA, playerB;
    private Integer nextStepA = null;
    private Integer nextStepB = null;
    private ReentrantLock lock = new ReentrantLock();
    private String status = "playing";  // playing finished
    private String loser = ""; // all, A, B

    private final static String addBotUrl = "http://127.0.0.1:3002/bot/add/";

    public Game(Integer rows, Integer cols, Integer innerWallsCount, Integer idA, Bot aBot, Integer idB, Bot bBot) {
        this.rows = rows;
        this.cols = cols;
        this.innerWallsCount = innerWallsCount;
        gameMap = new int[rows][cols];

        Integer aBotId = -1, bBotId = -1;
        String aBotCode = "", bBotCode = "";
        if(aBot != null) {
            aBotId = aBot.getId();
            aBotCode = aBot.getCode();
        }
        if(bBot != null) {
            bBotId = bBot.getId();
            bBotCode = bBot.getCode();
        }

        playerA = new Player(idA, aBotId, aBotCode, rows - 2, 1, new ArrayList<>());
        playerB = new Player(idB, bBotId, bBotCode, 1, cols - 2, new ArrayList<>());
    }

    private String mapToString(){
        StringBuilder res = new StringBuilder();
        for(int i = 0; i < rows; i++){
            for(int j = 0; j < cols; j++){
                res.append(gameMap[i][j]);
            }
        }
        return res.toString();
    }

    public void setNextStepA(Integer nextStepA) {
        lock.lock();
        try{
            this.nextStepA = nextStepA;
        } finally {
            lock.unlock();
        }
    }
    public void setNextStepB(Integer nextStepB) {
        lock.lock();
        try{
            this.nextStepB = nextStepB;
        } finally {
            lock.unlock();
        }
    }

    private boolean checkConnect(int sx, int sy, int tx, int ty){
        if(sx == tx && sy == ty) return true;
        gameMap[sx][sy] = 1;
        for(int i = 0; i < 4; i++){
            int x = sx + dx[i], y = sy + dy[i];
            if(x >= 0 && x < rows && y >= 0 && y < cols && gameMap[x][y] == 0){
                if(checkConnect(x,y,tx,ty)){
                    gameMap[sx][sy] = 0;
                    return true;
                }
            }
        }
        gameMap[sx][sy] = 0;
        return false;
    }
    private boolean createWalls(){
        for(int i = 0; i < rows; i++){
            for(int j = 0; j < cols; j++){
                gameMap[i][j] = 0;
            }
        }
        for(int r = 0; r < rows; r++){
            gameMap[r][0] = gameMap[r][cols - 1] = 1;
        }
        for(int c = 0; c < cols; c++){
            gameMap[0][c] = gameMap[rows - 1][c] = 1;
        }
        Random rand = new Random();
        for(int i = 0; i < innerWallsCount / 2; i++){
            for(int j = 0; j < 1000; j++){
                int r = rand.nextInt(rows);
                int c = rand.nextInt(cols);
                if(gameMap[r][c] == 1 || gameMap[rows - 1 - r][cols - 1 - c] == 1){
                    continue;
                }
                if(r == rows - 2 && c == 1 || r == 1 && c == cols - 2){
                    continue;
                }
                gameMap[r][c] = gameMap[rows - 1 - r][cols - 1 - c] = 1;
                break;
            }
        }
        return checkConnect(rows - 2,  1, 1, cols - 2);
    }
    public void createGameMap(){
        for(int i = 0; i < 1000; i ++){
            if(createWalls()){
                break;
            }
        }
    }

    private String getInput(Player player){   //  将当前的局面信息 编码成字符串
        Player me, you;
        if(Objects.equals(player.getId(), playerA.getId())){
            me = playerA;
            you = playerB;
        } else {
            me = playerB;
            you = playerA;
        }
        return mapToString() + '#' +
                me.getSx().toString() + '#' +
                me.getSy().toString() + "#(" +
                me.stepsToString() + ")#" +
                you.getSx().toString() + '#' +
                you.getSy().toString() + "#(" +
                you.stepsToString() + ")#";
    }
    private void sendBotCode(Player player){
        if(player.getBotId() == -1)return;
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("userId", player.getId().toString());
        data.add("botCode", player.getBotCode());
        data.add("input", getInput(player));
        String resp = WebSocketServer.restTemplate.postForObject(addBotUrl, data, String.class);
        System.out.println(resp);
    }
    private boolean nextStep(){    // 等待两名玩家下一步操作
        try{        //  前端蛇的移动比较慢，等前端
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sendBotCode(playerA);
        sendBotCode(playerB);

        for(int i = 0; i < 50; i++){
            try{
                Thread.sleep(100);
                lock.lock();
                try{
                    if(nextStepA !=null && nextStepB != null){
                        playerA.getSteps().add(nextStepA);
                        playerB.getSteps().add(nextStepB);
                        return true;
                    }
                } finally {
                    lock.unlock();
                }
            } catch(InterruptedException e){
                e.printStackTrace();
            }
        }
        return false;
    }
    private void sendAllMessage(String message){
        if(WebSocketServer.users.get(playerA.getId()) != null){
            WebSocketServer.users.get(playerA.getId()).sendMessage(message);
        }
        if(WebSocketServer.users.get(playerB.getId()) != null){
            WebSocketServer.users.get(playerB.getId()).sendMessage(message);
        }
    }
    private void sendResult(){
        JSONObject resp = new JSONObject();
        resp.put("event", "result");
        resp.put("loser", loser);
        saveRecordToDataBase();
        sendAllMessage(resp.toJSONString());
    }
    private void sendMove(){
        lock.lock();
        try{
            JSONObject resp = new JSONObject();
            resp.put("event", "move");
            resp.put("a_move", nextStepA);
            resp.put("b_move", nextStepB);
            nextStepA = null;
            nextStepB = null;
            sendAllMessage(resp.toJSONString());
        } finally {
            lock.unlock();
        }
    }
    private boolean checkValid(List<Cell> cellsA, List<Cell> cellsB){
        int n = cellsA.size();
        Cell cell = cellsA.get(n - 1);
        if(gameMap[cell.getX()][cell.getY()] == 1){
            return false;
        }
        for(int i = 0; i < n - 1; i++){
            if(cellsA.get(i).getX() == cell.getX() && cellsA.get(i).getY() == cell.getY()){
                return false;
            }
        }
        for(int i = 0; i < n - 1; i++){
            if(cellsB.get(i).getX() == cell.getX() && cellsB.get(i).getY() == cell.getY()){
                return false;
            }
        }
        return true;
    }
    private void judge(){
        List<Cell> cellsA = playerA.getCells();
        List<Cell> cellsB = playerB.getCells();

        boolean validA = checkValid(cellsA, cellsB);
        boolean validB = checkValid(cellsB, cellsA);
        if(!validA || !validB){
            status = "finished";
            if(!validA && !validB){
                loser = "all";
            } else if(!validA){
                loser = "A";
            } else if(!validB){
                loser = "B";
            }
        }
    }

    private void saveRecordToDataBase(){
        Integer aRating = WebSocketServer.userMapper.selectById(playerA.getId()).getRating();
        Integer bRating = WebSocketServer.userMapper.selectById(playerB.getId()).getRating();
        if("A".equals(loser)){
            aRating -= 2;
            bRating += 5;
        } else if("B".equals(loser)){
            aRating += 5;
            bRating -= 2;
        }
        updateUserRating(playerA, aRating);
        updateUserRating(playerB, bRating);

        Record record = new Record(
                null,
                playerA.getId(),
                playerA.getSx(),
                playerA.getSy(),
                playerB.getId(),
                playerB.getSx(),
                playerB.getSy(),
                playerA.stepsToString(),
                playerB.stepsToString(),
                mapToString(),
                loser,
                new Date()
        );
        WebSocketServer.recordMapper.insert(record);
    }

    private void updateUserRating(Player player, Integer rating){
        User user = WebSocketServer.userMapper.selectById(player.getId());
        user.setRating(rating);
        WebSocketServer.userMapper.updateById(user);
    }

    @Override
    public void run() {
        try {
            Thread.sleep(2000);  //  前端有两秒跳转的时间
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        for(int i = 0; i < 1000; i++){   // 最多1000回合一定结束
            if(nextStep()){
                judge();
                if("playing".equals(status)){
                    sendMove();
                } else {
                    sendResult();
                    break;
                }
            } else {
                status = "finished";
                lock.lock();
                try{
                    if(nextStepA == null && nextStepB == null){
                        loser = "all";
                    } else if(nextStepA == null){
                        loser = "A";
                    } else {
                        loser = "B";
                    }
                } finally {
                    lock.unlock();
                }
                sendResult();
                break;
            }
        }

    }
}
