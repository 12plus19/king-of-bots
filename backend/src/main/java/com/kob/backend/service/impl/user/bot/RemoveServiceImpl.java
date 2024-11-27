package com.kob.backend.service.impl.user.bot;

import com.kob.backend.mapper.BotMapper;
import com.kob.backend.pojo.Bot;
import com.kob.backend.pojo.User;
import com.kob.backend.service.impl.UserDetailsImpl;
import com.kob.backend.service.user.bot.RemoveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service("BotRemoveServiceImpl")
public class RemoveServiceImpl implements RemoveService {

    @Autowired
    private BotMapper botMapper;

    @Override
    public Map<String, String> remove(Map<String, String> data) {
        UsernamePasswordAuthenticationToken authenticationToken =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl loginUser = (UserDetailsImpl) authenticationToken.getPrincipal();
        User user = loginUser.getUser();
        Map<String, String> map = new HashMap<>();

        int bot_id = Integer.parseInt(data.get("id"));
        Bot bot = botMapper.selectById(bot_id);
        if(bot == null){
            map.put("error_msg", "Bot不存在或已经被删除");
            return map;
        }
        if(!Objects.equals(bot.getUserId(), user.getId())){
            map.put("error_msg", "没有权限删除该Bot");
            return map;
        }
        botMapper.deleteById(bot_id);
        map.put("error_msg", "success");
        return map;
    }
}
