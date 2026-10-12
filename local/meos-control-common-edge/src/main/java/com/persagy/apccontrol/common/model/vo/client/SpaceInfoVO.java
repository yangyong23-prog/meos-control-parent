package com.persagy.apccontrol.common.model.vo.client;

import lombok.Data;

/**
 * @ClassName 薛涛
 * @Description CS端空间详细数据
 * @Author
 * @Date 2024/7/23 14:02
 * @Version 1.0
 */
@Data
public class SpaceInfoVO {

    private String id;

    private String localId;

    private String localName;


    private String projectId;


    private String objType;

    private String classCode;

    private String groupCode;

    private String buildingId;

    private String floorId;


    private Object buildArea;

    private String buildingName;

    private String buildingLocalName;

    private String floorLocalId;

    private String floorLocalName;

    private Integer floorSequenceID;



}
