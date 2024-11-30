package com.kob.backend.consumer;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Player {
    private Integer id;
    private Integer botId;  // -1是人
    private String botCode;
    private Integer sx;
    private Integer sy;
    private List<Integer> steps;

    private boolean checkTailIncreasing(int step){             //  检验蛇是否变长，因为判断合法需要蛇的身体
        return step % 3 == 1;
    }

    public List<Cell> getCells(){
        List<Cell> res =  new ArrayList<>();
        int[] dx = {-1, 0, 1, 0}, dy = {0, 1, 0, -1};
        int step = 0;
        int x = sx, y = sy;
        res.add(new Cell(x,y));
        for(int d: steps){
            step++;
            x += dx[d];
            y += dy[d];
            res.add(new Cell(x,y));
            if(!checkTailIncreasing(step)){
                res.remove(0);
            }
        }
        return res;
    }

    public String stepsToString(){
        StringBuilder res = new StringBuilder();
        for(int d : steps){
            res.append(d);
        }
        return res.toString();
    }
}
