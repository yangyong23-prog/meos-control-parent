package com.persagy.apccontrol.basic.model.dto;

import com.persagy.apccontrol.basic.model.enumtype.DataSynRunEnum;
import com.persagy.apccontrol.basic.model.enumtype.SyncStatusEnum;
import lombok.Data;

import java.util.List;

/**
 * 同步数据标识
 *
 * @author yuyao
 */
@Data
public class DataSynDTO {

    /**
     * 同步表名称
     */
    private DataSynRunEnum tableName;

    /**
     * 项目id
     */
    private String projectId;


    /**
     * 是否全量同步 默认是 fasle
     */
    private boolean all = false;

    /**
     * 获取待同步的数据
     */
    private List<String> ysnObj;

    /**
     * 操作人
     */
    private String operatorName;

    /**
     * 操作时间
     */
    private String operateTime;

    /**
     * 同步状态
     */
    private SyncStatusEnum syncStatus = SyncStatusEnum.NONE;

    /**
     * 错误信息
     */
    private String errorMessage;

}
