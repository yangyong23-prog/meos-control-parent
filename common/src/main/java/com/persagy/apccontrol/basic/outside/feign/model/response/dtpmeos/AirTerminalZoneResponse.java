package com.persagy.apccontrol.basic.outside.feign.model.response.dtpmeos;

import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * 查询-冰蓄冷需求计算结果
 *
 * @author JiYinchuan
 * @since 7.260201.0
 */
@Data
public class AirTerminalZoneResponse  {

    /**
     * 泵组list
     */
    private List<PumGroup> pumpGroups;

    /**
     * 空调分区id
     */
    private String airTerminalZoneId;

    /**
     * 空调分区名称
     */
    private String airTerminalZoneName;

    /**
     * 分区开关编码
     */
    private String switchSetInfoCode;

    /**
     * 分区开关编码名称
     */
    private String switchSetInfoCodeName;


}
