package com.kob.backend.service.impl.user.bot;

import com.kob.backend.mapper.BotMapper;
import com.kob.backend.pojo.Bot;
import com.kob.backend.pojo.User;
import com.kob.backend.service.impl.UserDetailsImpl;
import com.kob.backend.service.user.bot.UpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class UpdateServiceImpl implements UpdateService {

    @Autowired
    private BotMapper botMapper;

    @Override
    public Map<String, String> update(Map<String, String> data) {
        UsernamePasswordAuthenticationToken authenticationToken =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl loginUser = (UserDetailsImpl) authenticationToken.getPrincipal();
        User user = loginUser.getUser();

        int bot_id = Integer.parseInt(data.get("id"));
        String botName = data.get("botName");
        String description = data.get("description");
        String code = data.get("code");
        Bot bot = botMapper.selectById(bot_id);

        Map<String, String> map = new HashMap<>();

        if(bot == null){
            map.put("error_msg", "Bot不存在或已经被删除");
            return map;
        }
        if(!Objects.equals(bot.getUserId(), user.getId())){
            map.put("error_msg", "没有权限修改该Bot");
            return map;
        }

        if(botName == null || botName.isEmpty()){
            map.put("error_msg", "机器人名称不能为空");
            return map;
        }
        if(botName.length() > 100){
            map.put("error_msg", "机器人名称不能大于100");
            return map;
        }
//        if(description == null || description.isEmpty()){
//            description = "这个用户很懒，什么也没留下~~";
//        }
        if(description.length() > 300){
            map.put("error_msg", "描述不能大于300");
            return map;
        }
        if(code == null || code.isEmpty()){
            map.put("error_msg", "代码不能为空");
            return map;
        }
        if(code.length() > 10000){
            map.put("error_msg", "代码长度不能超过10000");
            return map;
        }

        Bot new_bot = new Bot(
                bot.getId(),
                user.getId(),
                botName,
                description,
                code,
                bot.getRating(),
                bot.getCreateTime(),
                new Date()
        );
        botMapper.updateById(new_bot);
        map.put("error_msg", "success");
        return map;
    }
}
