package com.persagy.apccontrol.basic.model.context;

import com.persagy.apccontrol.basic.model.dto.DataSynDTO;
import lombok.Data;

/**
 * 数据同步上下文
 *
 * @author yuyao
 */
@Data
public class SynDataCtx {

    /**
     * 项目ID
     */
    private String projectId;

    /**
     * 是否是接口同步 不是就发数MQ消息
     */
    private boolean isPortSyn;

    /**
     * 同步调用地址
     */
    private String synUrl;

    /**
     * 获取待同步的数据
     * 一次同步一张表
     */
    private DataSynDTO ysnObj;
}
