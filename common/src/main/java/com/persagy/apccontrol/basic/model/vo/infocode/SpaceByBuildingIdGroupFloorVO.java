package com.persagy.apccontrol.basic.model.vo.infocode;

import lombok.Data;

import java.util.List;

/**
 * @ClassName 薛涛
 * @Author Jiangnan Cui
 * @Date 2024/6/28 18:29
 * @Version 1.0
 */
@Data
public class SpaceByBuildingIdGroupFloorVO  {
    /**
     * 楼层ID
     */
    private String floorId;
    /**
     * 楼层本地名称
     */
    private String floorLocalName;

    /**
     * 楼层本地编码
     */
    private String floorLocalId;

    private List<InfoCodeObjectGroupVO> spaces;
}
