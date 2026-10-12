package com.persagy.apccontrol.basic.model.vo.intervention;

import lombok.Data;

/**
 * 干预配置详情
 *
 * @author yuecaipu
 * @date 2024/7/23 16:15
 * @since v5.240402.00
 */
@Data
public class ConfigDetailsVo {

    /**
     * 干预起始时间
     */
    private String beginTime;

    /**
     * 干预配置数据
     */
    private ConfigData configData;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 干预原因
     */
    private String reason;
}
