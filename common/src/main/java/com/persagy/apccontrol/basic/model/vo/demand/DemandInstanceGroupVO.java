package com.persagy.apccontrol.basic.model.vo.demand;

import com.persagy.apccontrol.basic.model.dto.demand.DemandBelongInstanceDTO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandInstanceGroupDetail;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode
public class DemandInstanceGroupVO {

    /**
     * 所属实例类型
     */
    private DemandBelongInstanceDTO belongInstance;
    /**
     * 编组ID
     */
    private Long id;
    /**
     * 模板类型
     */
    private DemandTemplateTypeEnum templateType;
    /**
     * 编组名称
     */
    private String name;
    /**
     * 编组用途
     */
    private String purpose;
    /**
     * 关联对象
     */
    private List<DemandInstanceGroupDetailVO> relObjects;
    /**
     * 关联详情对象
     */
    private List<DemandInstanceGroupDetail> details;
    /**
     * 编组标签
     */
    private String tag;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
