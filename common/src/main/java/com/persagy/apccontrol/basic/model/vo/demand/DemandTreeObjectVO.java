package com.persagy.apccontrol.basic.model.vo.demand;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * 需求管理-对象树VO
 *
 * @author yuyao
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "需求管理-对象树VO")
public class DemandTreeObjectVO extends DtpRwdSpaceDTO {

    @Schema(description = "子节点")
    private List<DemandTreeObjectVO> children;

    //配置信息
    @Schema(description = "标签信息")
    private DemandInstanceTagVO instanceTag;

    // ------------------------- 楼层字段 -------------------------

    @Schema(description = "层高")
    private BigDecimal height;

    @Schema(description = "楼板标高")
    private BigDecimal floorElevation;

    public DemandInstanceTypeEnum getInstanceType() {
        if (StringUtils.isNotBlank(this.getClassCode())) {
            return DemandInstanceTypeEnum.create(BaseEnum.analyze(
                    this.getClassCode(), DtpRwdClassCodeEnum.class, false));
        }
        //项目无 classCode
        return DemandInstanceTypeEnum.PROJECT;
    }

    /**
     * 获取实例类型和实例映射
     *
     * @param target 目标源
     * @param source 数据源
     * @return 实例类型 -> 实例对象
     * @since 5.241102.0
     */
    public static Set<DemandInstanceValueObject> listAllDemandInstances(
            @NonNull Set<DemandInstanceValueObject> target,
            @NonNull List<DemandTreeObjectVO> source) {
        if (CollectionUtils.isEmpty(source)) {
            return target;
        }
        for (DemandTreeObjectVO vo : source) {
            target.add(new DemandInstanceValueObject(vo.getInstanceType(), vo.getId()));
            List<DemandTreeObjectVO> children = vo.getChildren();
            if (CollectionUtils.isNotEmpty(children)) {
                target = listAllDemandInstances(target, children);
            }
        }
        return target;
    }
}
