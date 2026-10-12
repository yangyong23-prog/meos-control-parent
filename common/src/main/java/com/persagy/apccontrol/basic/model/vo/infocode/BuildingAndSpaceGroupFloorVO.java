package com.persagy.apccontrol.basic.model.vo.infocode;

import lombok.Data;

import java.util.List;

/**
 * @ClassName 薛涛
 * @Author
 * @Date 2024/6/28 18:29
 * @Version 1.0
 */
@Data
public class BuildingAndSpaceGroupFloorVO {
    /**
     * 建筑ID
     */
    private String buildingId;

    /**
     * 建筑名称
     */
    private String buildingName;
    /**
     * 建筑本地名称
     */
    private String buildingLocalName;

    /**
     * 建筑本地编码
     */
    private String buildingLocalId;



    /** 楼层*/
    private List<SpaceByBuildingIdGroupFloorVO> floors;
}
