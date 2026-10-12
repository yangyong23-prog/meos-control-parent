package com.persagy.apccontrol.basic.model.calculate;

import cn.hutool.core.util.StrUtil;
import com.persagy.apccontrol.basic.model.enumtype.EnumCalculateValueType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 价值计算--结果详情
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/9/7 18:43
 */
@Getter
@Setter
@ToString
public class CalculateResultDetail implements Serializable {

    private static final long serialVersionUID = -2940295230485784751L;

    // ------------------------- 配置数据 ----------------------
    @Schema(description = "最大值")
    private BigDecimal max;

    @Schema(description = "最小值")
    private BigDecimal min;

    @Schema(description = "目标值")
    private BigDecimal target;

    // ------------------------- 价值计算对结果 -------------------
    @Schema(description = "是否有需求")
    private Boolean enable;

    @Schema(description = "数值")
    private BigDecimal value;

    @Schema(description = "数值类型")
    private String type;

    @Schema(description = "数值类型名称")
    private String typeName;

    public String getTypeName() {
        if (StrUtil.isNotBlank(type)) {
            EnumCalculateValueType analyze = EnumCalculateValueType.analyze(type);
            return analyze == null ? null : analyze.getDesc();
        }
        return null;
    }

    public CalculateResultDetail() {

    }

    public CalculateResultDetail(Boolean enable) {
        this.enable = enable;
    }

}
