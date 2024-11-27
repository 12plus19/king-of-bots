package com.kob.backend.service.impl.friends;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kob.backend.mapper.FriendsMapper;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.Friends;
import com.kob.backend.pojo.User;
import com.kob.backend.service.friends.GetListService;
import com.kob.backend.service.impl.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("FriendsGetListServiceImpl")
public class GetListServiceImpl implements GetListService {
    @Autowired
    private FriendsMapper friendsMapper;
    @Autowired
    private UserMapper userMapper;
    @Override
    public List<User> getList() {
        UsernamePasswordAuthenticationToken authenticationToken =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl loginUser = (UserDetailsImpl) authenticationToken.getPrincipal();
        User user = loginUser.getUser();

        QueryWrapper<Friends> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", user.getId());
        List<Friends> temp = friendsMapper.selectList(queryWrapper);
        List<User> res = new ArrayList<>();
        for(Friends friend : temp) {
            User user1 = userMapper.selectById(friend.getFriendId());
            res.add(user1);
        }

        return res;
    }
}
