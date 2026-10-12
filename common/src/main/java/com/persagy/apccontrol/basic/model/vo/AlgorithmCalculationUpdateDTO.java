package com.persagy.apccontrol.basic.model.vo;

import cn.hutool.core.collection.CollectionUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.math3.analysis.function.Floor;

import java.util.List;

/**
 * @Description: 算法修改参数
 * @author: yancheng
 * @date: 2023/3/25
 * @version: V1.0
 */
@Data
@Schema(title = "算法修改参数")
@NoArgsConstructor
@AllArgsConstructor
public class AlgorithmCalculationUpdateDTO {

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "系统编码")
    private String systemId;

    @Schema(description = "室内温度计算规则")
    private IndoorTempRule indoorTempRule;

    @Schema(description = "最大加减冷机挡位限制 暂无默认传递1L0S")
    private String gearLimit = "1L0S";

    @Schema(description = "是否支持同时开启大小机 暂无默认传递true")
    private Boolean sameTimeOpenLSChiller = true;

    @Schema(description = "专家经验调研")
    private List<ExpertExperienceDTO> expertExperience;

    public void setTempRule() {
        IndoorTempRule indoorTempRule = new IndoorTempRule();
        Target target = new Target();
        target.setRemoveObject(CollectionUtil.newArrayList());
        TargetCalRule targetCalRule = new TargetCalRule();
        targetCalRule.setAllBuilding(new AllBuilding());
        targetCalRule.setBuilding(new Building());
        targetCalRule.setFloor(new Floor());
        target.setTargetCalRule(targetCalRule);
        indoorTempRule.setTarget(target);
        this.indoorTempRule = indoorTempRule;
    }

}
