package com.persagy.apccontrol.basic.resource.demand.param.lighting.scene;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandLightingSceneTimeIntervalDTO;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 需求配置-照明需求-场景保存参数
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@Accessors(chain = true)
public class DemandConfigLightingSceneSaveDaoParam implements BaseCheckParamProvider {

    /**
     * 开始时间
     */
    @NotNull(message = "{key_8ceaf15b76524876a9b10eea2e267297}")
    @Valid
    private DemandLightingSceneTimeIntervalDTO beginTime;

    /**
     * 结束时间
     */
    @NotNull(message = "{key_4e2d94f301f84fa18b03b7ec21f4a3d7}")
    @Valid
    private DemandLightingSceneTimeIntervalDTO endTime;

    /**
     * 场景名称
     */
    @NotBlank(message = "{key_364c43d7367446ba9e8ff60070ec270d}")
    private String sceneName;

    /**
     * 排序
     */
    @NotNull(message = "{key_061d52c923dc4c7abf97f05c2398cc26}")
    private Integer sequence;

    /**
     * 关联需求模版ID
     */
    @NotNull(message = "{key_8c0f8d5f23034cb09375574633685fb9}")
    private Long relDemandTemplateId;

    /**
     * 状态
     */
    @NotNull(message = "{key_31b1c9f4e49941dd933a4f97efcd1a5a}")
    private StateTypeEnum st = StateTypeEnum.VALID;

    @Override
    public void checkAndSet() {
        beginTime.checkAndSet();
        endTime.checkAndSet();
    }
}
