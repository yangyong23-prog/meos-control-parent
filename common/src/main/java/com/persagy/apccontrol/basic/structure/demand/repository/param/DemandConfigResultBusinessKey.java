package com.persagy.apccontrol.basic.structure.demand.repository.param;

import com.persagy.apccontrol.basic.model.entity.demand.DemandConfigResult;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * 需求配置预计算结果业务键
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DemandConfigResultBusinessKey {

    /**
     * 需求模板类型
     */
    private @NonNull DemandTemplateTypeEnum demandTemplateType;

    /**
     * 需求实例类型
     */
    private @NonNull DemandInstanceTypeEnum demandInstanceType;

    /**
     * 需求实例ID
     */
    private @NonNull String demandInstanceId;

    /**
     * 关联需求模板ID
     */
    private @NonNull Long relTemplateId;

    /**
     * 关联需求场景ID
     */
    private @Nullable Long relSceneId;

    /**
     * 从结果实体创建业务键
     *
     * @param result 预计算结果
     * @return 业务键
     * @since v7.260701.0
     */
    public static @NonNull DemandConfigResultBusinessKey of(@NonNull DemandConfigResult result) {
        DemandConfigResultBusinessKey businessKey = new DemandConfigResultBusinessKey();
        businessKey.setDemandTemplateType(result.getDemandTemplateType());
        businessKey.setDemandInstanceType(result.getDemandInstanceType());
        businessKey.setDemandInstanceId(result.getDemandInstanceId());
        businessKey.setRelTemplateId(result.getRelTemplateId());
        businessKey.setRelSceneId(result.getRelSceneId());
        return businessKey;
    }
}
