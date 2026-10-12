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
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AirTerminalZoneControllerParameterResponse extends BaseResponse {

    /**
     * 空调分区list
     */
    private List<AirTerminalZoneResponse> airTerminalZones;


}
