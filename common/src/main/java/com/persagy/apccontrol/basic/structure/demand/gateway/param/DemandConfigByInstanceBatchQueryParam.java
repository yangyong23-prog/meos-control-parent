package com.persagy.apccontrol.basic.structure.demand.gateway.param;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

/**
 * 需求配置根据实例查询参数
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Data
public class DemandConfigByInstanceBatchQueryParam implements BaseCheckParamProvider {

    /**
     * 查询日期, 不为 {@literal null} 且为历史日期时查询快照表, 否则查询当前配置
     */
    private LocalDate date;

    /**
     * 模版ID
     */
    @NotNull(message = "{key_02bebf76336942da9503b791f451570d}")
    private Long templateId;

    /**
     * 场景ID, 为空时查询所有
     */
    private Long sceneId;

    /**
     * 配置组ID
     */
    private Set<Long> configGroupIds;

    /**
     * 需求实例类型 -> 需求实例ID, 需求实例ID为空时查询所有
     */
    private Map<DemandInstanceTypeEnum, Set<String>> instanceTypeAndInstanceIds;

    /**
     * 是否自动填充空的实例ID, 为 {@literal true} 时 {@code instanceTypeAndInstanceIds} 将自动填充为空的实例ID
     */
    private boolean autoFillEmptyInstanceIds = false;

    /**
     * 是否查询继承配置, 为 {@literal true} 时将查询继承配置
     */
    private boolean queryInheritConfig = true;

    /**
     * 是否过滤屏蔽空间
     */
    private boolean filterShieldedSpaces = false;

    /**
     * 是否填充实例名称
     */
    private boolean fillInstanceName = true;

    @Override
    public void checkAndSet() {
        if (CollectionUtils.isEmpty(this.configGroupIds) && MapUtils.isEmpty(this.instanceTypeAndInstanceIds)) {
            this.instanceTypeAndInstanceIds = DemandInstanceTypeEnum.toAllInstanceTypeAndInstanceIds();
        }
    }
}
