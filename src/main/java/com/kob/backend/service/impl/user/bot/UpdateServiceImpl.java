package com.kob.backend.service.impl.user.bot;

import com.kob.backend.service.user.bot.UpdateService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UpdateServiceImpl implements UpdateService {

    @Override
    public Map<String, String> updateBot(Map<String, String> data) {
        return Map.of();
    }
}
