package com.persagy.apccontrol.basic.model.dto;

import lombok.Data;



/**
 * iot孪生变更推送数据
 *
 * @author yuecaipu
 * @date 2024/6/24 19:27
 * @since v5.240602.00
 */
@Data
public class ObjectIotData {
    /**对象实例Id */
    private String objectId;
    /** 消息点编码 */
    private String infoCode;
    /** 单位 */
    private String unit;
    /**孪生值  */
    private String dataValue;
    /**孪生值  */
    private String dataValueName;
    /**时间  */
    private String dataTime;
    /**时间常量  */
    private String dataIndate;

}
