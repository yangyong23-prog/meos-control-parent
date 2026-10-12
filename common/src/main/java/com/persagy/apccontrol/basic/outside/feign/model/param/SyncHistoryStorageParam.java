package com.persagy.apccontrol.basic.outside.feign.model.param;

import com.persagy.apccontrol.basic.outside.enums.StorageTagEnum;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class SyncHistoryStorageParam {

    private LocalDate date;

    private List<RecordContent> recordContent;

    @Data
    public static class RecordContent {

        private LocalTime time;

        private StorageTagEnum storageTag;

    }
}
