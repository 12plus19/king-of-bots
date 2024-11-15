package com.kob.backend.controller.user.bot;

import com.kob.backend.service.user.bot.GetSignatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class GetSignatureController {

    @Autowired
    private GetSignatureService getSignatureService;

    @GetMapping("/user/bot/getsignature/")
    public Map<String, String> getSignature(){
        return getSignatureService.getSignature();
    }
}
