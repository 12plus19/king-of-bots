package com.kob.backend.service.impl.user.bot;

import com.kob.backend.mapper.BotMapper;
import com.kob.backend.pojo.Bot;
import com.kob.backend.pojo.User;
import com.kob.backend.service.impl.UserDetailsImpl;
import com.kob.backend.service.user.bot.AddService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class AddServiceImpl implements AddService {

    @Autowired
    private BotMapper botMapper;
    @Override
    public Map<String, String> add(Map<String, String> bot) {
        UsernamePasswordAuthenticationToken authenticationToken =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl loginUser = (UserDetailsImpl) authenticationToken.getPrincipal();
        User user = loginUser.getUser();

        String botName = bot.get("botName");
        String description = bot.get("description");
        String code = bot.get("code");

        Map<String, String> map = new HashMap<>();

        if(botName == null || botName.isEmpty()){
            map.put("error_msg", "机器人名称不能为空");
            return map;
        }
        if(botName.length() > 100){
            map.put("error_msg", "机器人名称不能大于100");
            return map;
        }
        if(description == null || description.isEmpty()){
            description = "这个用户很懒，什么也没留下~~";
        }
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
        Date now = new Date();
        Bot newBot = new Bot(null, user.getId(), botName, description, code, 400, now, now);
        botMapper.insert(newBot);
        map.put("error_msg", "success");

        return map;
    }
}
