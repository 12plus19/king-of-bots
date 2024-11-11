package com.kob.backend.service.impl.user.bot;

import com.kob.backend.service.user.bot.RemoveService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RemoveServiceImpl implements RemoveService {

    @Override
    public Map<String, String> removeBot(Map<String, String> bot) {
        return Map.of();
    }
}
