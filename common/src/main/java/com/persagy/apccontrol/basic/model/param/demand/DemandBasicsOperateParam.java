package com.persagy.apccontrol.basic.model.param.demand;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Set;

/**
 * 需求基础操作查询参数
 */
@Data
@ToString(callSuper = true)
@Schema(title = "需求基础操作查询参数")
public class DemandBasicsOperateParam implements BaseCheckParamProvider {

    @Schema(description = "实例类型")
    @NotNull(message = "{key_3b13463321154c7695bc97753eeede5e}")
    private DemandInstanceTypeEnum instanceType;

    /**
     * 实例ID
     *
     * @deprecated 暂时保留, 以兼容除空调需求外的其他需求配置
     */
    @Deprecated
    @Schema(description = "实例ID")
    private String instanceId;

    @Schema(description = "实例ID")
    private Set<String> instanceIds;

    @Schema(description = "模板ID")
    @NotNull(message = "{key_a8a0a53f475f4137af42b23076047014}")
    private Long templateId;

    @Schema(description = "场景ID,有就必传")
    private Long sceneId;

    @Schema(description = "配置分组ID")
    private Long configGroupId;

    /**
     * 获取实例ID
     *
     * @return 实例ID
     * @since 7.250202.0
     */
    public Set<String> getInstanceIds() {
        if (CollectionUtils.isNotEmpty(this.instanceIds)) {
            return this.instanceIds;
        }
        if (StringUtils.isNotBlank(this.instanceId)) {
            return Sets.newHashSet(this.instanceId);
        }
        return null;
    }

    @Override
    public void checkAndSet() {
        if (Objects.isNull(configGroupId) && (Objects.isNull(instanceType) && CollectionUtils.isEmpty(getInstanceIds()))) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_5bf6c538bc484187b87836c4512fe161"));
        }
    }
}
