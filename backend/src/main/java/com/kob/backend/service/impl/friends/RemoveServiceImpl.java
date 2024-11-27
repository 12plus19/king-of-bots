package com.kob.backend.service.impl.friends;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kob.backend.mapper.FriendsMapper;
import com.kob.backend.pojo.Friends;
import com.kob.backend.pojo.User;
import com.kob.backend.service.friends.RemoveService;
import com.kob.backend.service.impl.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service("FriendRemoveServiceImpl")
public class RemoveServiceImpl implements RemoveService {

    @Autowired
    private FriendsMapper friendsMapper;
    @Override
    public Map<String, String> remove(Map<String, String> friend) {
        UsernamePasswordAuthenticationToken authenticationToken =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl loginUser = (UserDetailsImpl) authenticationToken.getPrincipal();
        User user = loginUser.getUser();
        Map<String, String> map = new HashMap<>();

        int friendId = Integer.parseInt(friend.get("friendId"));
        QueryWrapper<Friends> wrapper = new QueryWrapper<>();
        wrapper.eq("friend_id", friendId);
        wrapper.eq("user_id", user.getId());
        friendsMapper.delete(wrapper);
        map.put("error_msg", "success");
        return map;
    }
}
