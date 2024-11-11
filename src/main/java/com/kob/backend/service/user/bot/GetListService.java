package com.kob.backend.service.user.bot;

import com.kob.backend.pojo.Bot;

import java.util.List;

//   返回指定用户所有的 bot

public interface GetListService {
    List<Bot> getList();
}
