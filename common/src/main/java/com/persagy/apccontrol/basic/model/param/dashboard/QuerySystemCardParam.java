package com.persagy.apccontrol.basic.model.param.dashboard;

import lombok.Data;

/**
 * 查询系统卡片参数
 *
 * @author MuAnFu
 * @date 2025-06-05
 */
@Data
public class QuerySystemCardParam {

    /**
     * 是否查询所有实例，默认只返回有系统图的对象
     */
    private boolean allInstance = false;

    /**
     * 是否根据可监可控过滤,默认不过滤
     */
    private boolean isFilterMonitorStatus = false;
}
