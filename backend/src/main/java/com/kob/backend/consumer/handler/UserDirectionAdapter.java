package com.kob.backend.consumer.handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDirectionAdapter implements DirectionAdapter{
    private static final Logger logger = LoggerFactory.getLogger(UserDirectionAdapter.class);

    @Override
    public Integer convertDirection(String directionInput) {
        logger.info("用户输入方向: " + directionInput);
        switch (directionInput) {
            case "W":
                return 0; // 上
            case "A":
                return 3; // 左
            case "S":
                return 2; // 下
            case "D":
                return 1; // 右
            default:
                logger.error("未知的方向输入: " + directionInput);
                return null;
        }
    }
}
