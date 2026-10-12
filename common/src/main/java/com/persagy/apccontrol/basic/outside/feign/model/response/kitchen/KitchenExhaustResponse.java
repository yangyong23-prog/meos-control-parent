package com.persagy.apccontrol.basic.outside.feign.model.response.kitchen;


import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Data
@ToString(callSuper = true)
public class KitchenExhaustResponse extends BaseResponse {

    /**
     * 设备id
     */
    private String id;
    /**
     * 设备的localName
     */
    private String localName;
    /**
     * 设备所在楼层id
     */
    private String floorId;
    /**
     * 建筑id
     */
    private String buildingId;

    /**
     * 设备所在楼层localName
     */
    private String floorName;
    /**
     *建筑localName
     */
    private String buildingName;
    /**
     * 子级对象
     */
    private List<KitchenExhaustResponse> subItem;
    /**
     * 聚合数据
     */
    private Map<String, List<KitchenExhaustResponse>> aggregate;

}
