package com.persagy.apccontrol.basic.handler;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.persagy.apccontrol.basic.outside.feign.model.param.SyncHistoryStorageParam;

import java.util.ArrayList;
import java.util.List;

public class RecordContentHandler extends JacksonTypeHandler {

    public RecordContentHandler(Class<?> type) {
        super(type);
    }


    @Override
    protected List<SyncHistoryStorageParam.RecordContent> parse(String json) {
        List<SyncHistoryStorageParam.RecordContent> jsons = new ArrayList<>();
        try {
            jsons = getObjectMapper().readValue(json, new TypeReference<List<SyncHistoryStorageParam.RecordContent>>(){});
        } catch (Exception ignored) {
        }
        return jsons;
    }
}
