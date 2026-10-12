package com.persagy.apccontrol.common.model.vo.client;

import lombok.Data;

import java.util.List;

/**
 * @ClassName 薛涛
 * @Author
 * @Date 2024/7/21 11:25
 * @Version 1.0
 */
@Data
public class RelationObjectsAndSpaceVO {


    private String id;

    private String localId;

    private String localName;

    /**设备列表*/
    private List<EquipmentInfoCodes> equipments;
    /**空间列表*/
    private List<SpaceInfoVO> spaces;
}
