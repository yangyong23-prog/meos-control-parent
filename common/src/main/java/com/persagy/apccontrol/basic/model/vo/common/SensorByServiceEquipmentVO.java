package com.persagy.apccontrol.basic.model.vo.common;

import lombok.Data;

import java.util.List;

/**
 * @ClassName 薛涛
 * @Author Jiangnan Cui
 * @Date 2024/7/23 17:40
 * @Version 1.0
 */
@Data
public class SensorByServiceEquipmentVO {
    /**
     * 建筑ID
     */
    private String buildingId;
    /**
     * 建筑本地名称
     */
    private String buildingLocalName;
    /**
     * 类型编码
     */
    private String classCode;
    /**
     * 楼层ID
     */
    private String floorId;
    /**
     * 楼层本地名称
     */
    private String floorLocalName;
    /**
     * 对象ID
     */
    private String id;
    /**
     * 对象本地ID
     */
    private String localId;
    /**
     * 对象本地名称
     */
    private String localName;
    /**
     * 对象名称
     */
    private String name;
    /**
     * 对象类型
     */
    private String objType;
    /**
     * 项目ID
     */
    private String projectId;

    /** 所在空间*/
    private List<DTPRwdSpaceVO> spaces;
}
