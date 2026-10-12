package com.persagy.apccontrol.basic.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.persagy.apccontrol.basic.handler.RecordContentHandler;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.outside.feign.model.param.SyncHistoryStorageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.List;

/**
 * 蓄冷蓄热结果表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "history_storage_result", autoResultMap = true)
public class HistoryStorageResult extends BaseAssignIdDO {

    /**
     * 记录时间
     */
    @TableField(value = "record_date")
    private LocalDate recordDate;

    /**
     * 内容
     */
    @TableField(value = "content", typeHandler = RecordContentHandler.class)
    private List<SyncHistoryStorageParam.RecordContent> content;

    /**
     * 项目id
     */
    @TableField(value = "project_id")
    private String projectId;
}