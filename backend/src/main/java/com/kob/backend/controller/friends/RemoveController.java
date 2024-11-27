package com.kob.backend.controller.friends;

import com.kob.backend.service.friends.RemoveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController("FriendRemoveController")
public class RemoveController {
    @Autowired
    private RemoveService removeService;

    @PostMapping("/friends/remove/")
    public Map<String, String> remove(@RequestParam Map<String, String> friend){
        return removeService.remove(friend);
    }
}
