package com.persagy.apccontrol.common.model.vo.client;

import lombok.Data;

import java.util.List;


/**
 * @ClassName 薛涛
 * @Author Jiangnan Cui
 * @Date 2024/6/28 18:29
 * @Version 1.0
 */
@Data
public class EquipmentInfoCodeObjectGroupByTemplateVO {

    /**
     * 模版类型ID
     */
    private Long templateTypeId;
    /**
     * 模版类型名称
     */
    private String templateTypeName;

    private List<InfoCodeObjectGroupCSVO> objects;

}
