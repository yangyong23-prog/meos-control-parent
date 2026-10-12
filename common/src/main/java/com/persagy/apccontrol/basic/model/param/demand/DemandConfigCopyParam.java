package com.persagy.apccontrol.basic.model.param.demand;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;

/**
 * 需求配置复制参数
 *
 * @author JiYinchuan
 * @since 7.250902.0
 */
@Data
public class DemandConfigCopyParam implements BaseCheckParamProvider {

    @Schema(description = "来源模版ID")
    @NotNull(message = "FromTemplateId can not be null")
    private Long fromTemplateId;

    @Schema(description = "来源场景ID")
    private Long fromSceneId;

    @Schema(description = "目标模版ID")
    @NotNull(message = "ToTemplateId can not be null")
    private Long toTemplateId;

    @Schema(description = "目标场景ID")
    private Long toSceneId;

    @Override
    public void checkAndSet() {
        if (this.checkPairAnyNull(Pair.of(this.fromSceneId, this.toSceneId))) {
            throw new ValidationException("SceneId can not be null");
        }
    }
}
