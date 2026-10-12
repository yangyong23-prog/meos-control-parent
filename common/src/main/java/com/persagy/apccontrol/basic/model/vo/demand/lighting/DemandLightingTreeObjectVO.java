package com.persagy.apccontrol.basic.model.vo.demand.lighting;

import com.persagy.apccontrol.basic.model.vo.BaseEnumVO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 需求管理-照明管控对象VO
 *
 * @author yuyao
 */
@Data
@Schema(description = "需求管理-管控对象VO")
public class DemandLightingTreeObjectVO {

    @Schema(description = "对象ID")
    private String id;

    @Schema(description = "对象名称")
    private String name;

    @Schema(description = "对象本地名称")
    private String localName;

    @Schema(description = "对象类型")
    private DemandInstanceTypeEnum instanceType;

    @Schema(description = "是否已配置照明需求（项目，建筑，楼层，空间）")
    private Boolean isCfg;

    @Schema(description = "灯具类型列表")
    private List<BaseEnumVO> lampFuncTypeList;

    @Schema(description = "功能类型列表")
    private List<BaseEnumVO> funcTypeList;
    @Schema(description = "3D模型消费字段信息")
    private Map<String, Object> modelMap;


    @Schema(description = "子节点")
    private List<DemandLightingTreeObjectVO> children;

}
