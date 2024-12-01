package com.kob.backend.controller.record;

import com.alibaba.fastjson.JSONObject;
import com.kob.backend.service.record.GetRecordListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class GetRecordListController {
    @Autowired
    private GetRecordListService getRecordListService;
    @GetMapping("/record/getlist/")
    public JSONObject getRecordList(@RequestParam Map<String, String> data) {
        Integer pageNo = Integer.parseInt(data.get("page_no"));
        return getRecordListService.getRecordList(pageNo);
    }
}
