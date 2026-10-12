package com.persagy.apccontrol.basic.model.vo.common;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.vo.infocode.InfoCodeGroupVO;
import lombok.Data;

import java.util.List;

/**
 * @ClassName 薛涛
 * @Author Jiangnan Cui
 * @Date 2024/7/23 17:41
 * @Version 1.0
 */
@Data
public class EquipmentServiceSpaceVO {

    /**
     * 建筑ID
     */
    private String buildingId;
    /**
     * 类型编码
     */
    private String classCode;
    /**
     * 动态名称
     */
    private String dynamicName;
    /**
     * 楼层ID
     */
    private String floorId;
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

    /**
     * 空间功能类型
     */
    private String roomFuncType;


    /**
     * 未定义属性列表
     */
    private JSONObject undefinedProperties;

    /**
     * 关联信息点分组配置
     */
    private List<InfoCodeGroupVO> relInfoCodeGroupConfigs;

    /**
     * 建筑本地名称
     */
    private String buildingLocalName;
    /**
     * 楼层本地名称
     */
    private String floorLocalName;

}
