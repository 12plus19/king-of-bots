package com.kob.backend.controller.friends;

import com.kob.backend.pojo.User;
import com.kob.backend.service.friends.GetListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("FriendsGetListController")
public class GetListController {
    @Autowired
    private GetListService getListService;
    @GetMapping("/friends/getlist/")
    public List<User> getList(){
        return getListService.getList();
    }
}
