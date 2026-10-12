package com.persagy.apccontrol.common.model.vo.client;

import lombok.Data;

import java.util.List;

/**
 * @ClassName 薛涛
 * @Author Jiangnan Cui
 * @Date 2024/7/22 15:41
 * @Version 1.0
 */
@Data
public class EquipmentInfoCodes {


    private String id;

    private String LocalId;

    private String localName;

    private String classCode;
    private String objType;


    private String spaceLocalId;

    private String spaceId;

    private String spaceLocalName;

    private Integer floorSequenceID;

    private String floorLocalName;

    private String floorLocalId;

    private String floorId;

    private String buildingId;

    private String buildingName;

    private String buildingLocalName;

    private List<InfoCodeDataVO>  infoCodes;


}
