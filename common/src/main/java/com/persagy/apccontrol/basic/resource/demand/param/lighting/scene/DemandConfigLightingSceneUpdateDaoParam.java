package com.persagy.apccontrol.basic.resource.demand.param.lighting.scene;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandLightingSceneTimeIntervalDTO;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * 需求配置-照明需求-场景更新参数
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@Accessors(chain = true)
public class DemandConfigLightingSceneUpdateDaoParam implements BaseCheckParamProvider {

    /**
     * ID
     */
    @NotNull(message = "{key_3bbfbcb78c1744b5b14eac0557d4c399}")
    private Long id;

    /**
     * 开始时间
     */
    @Valid
    private DemandLightingSceneTimeIntervalDTO beginTime;

    /**
     * 结束时间
     */
    @Valid
    private DemandLightingSceneTimeIntervalDTO endTime;

    /**
     * 场景名称
     */
    private String sceneName;

    /**
     * 排序
     */
    private Integer sequence;

    /**
     * 状态
     */
    private StateTypeEnum st;

    @Override
    public void checkAndSet() {
        if (Objects.nonNull(beginTime)) {
            beginTime.checkAndSet();
        }
        if (Objects.nonNull(endTime)) {
            endTime.checkAndSet();
        }
    }
}
