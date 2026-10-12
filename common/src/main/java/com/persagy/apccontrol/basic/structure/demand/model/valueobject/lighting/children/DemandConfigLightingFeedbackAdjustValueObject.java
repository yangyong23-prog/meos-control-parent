package com.persagy.apccontrol.basic.structure.demand.model.valueobject.lighting.children;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.model.base.domain.ValueObject;
import lombok.Data;
import org.jspecify.annotations.NonNull;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Set;

/**
 * 照明需求空间反馈调节配置值对象
 *
 * @author JiYinchuan
 * @since 7.250801.0
 */
@Data
public class DemandConfigLightingFeedbackAdjustValueObject implements ValueObject, BaseCheckParamProvider {

    /**
     * 反馈调节的空间ID
     */
    @NotEmpty(message = "{key_9f5eb177d93c4c83bf06bb2e94fb4496}")
    private @NonNull Set<@NonNull String> spaceIds;

    /**
     * 功能类型配置
     */
    @Valid
    @NotEmpty(message = "{key_af777baca0454befa28d0cfa68da30f2}")
    private @NonNull List<@NonNull DemandConfigLightingFuncTypeValueObject> funcTypeSettings;

    @Override
    public void checkAndSet() {
        for (DemandConfigLightingFuncTypeValueObject funcTypeSetting : this.funcTypeSettings) {
            funcTypeSetting.checkAndSet();
        }
    }
}
