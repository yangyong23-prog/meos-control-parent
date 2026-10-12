package com.persagy.apccontrol.basic.structure.demand.repository.param;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Set;

/**
 * 需求管理-需求配置新增/编辑参数
 *
 * @author JiYinchuan
 * @since 7.250202.0
 */
@Data
public class DemandConfigAddOrEditParam implements BaseCheckParamProvider {

    /**
     * 是否发布事件
     */
    private boolean publishEvent = true;

    /**
     * 模版ID
     */
    @NotNull(message = "{key_02bebf76336942da9503b791f451570d}")
    private Long templateId;

    /**
     * 场景ID, 存在场景时必传
     */
    private Long sceneId;

    /**
     * 分组配置ID
     */
    private Long configGroupId;

    /**
     * 实例类型
     */
    private DemandInstanceTypeEnum instanceType;

    /**
     * 实例ID
     */
    private Set<String> instanceIds;

    /**
     * 配置
     */
    @NotNull(message = "{key_52056cc794e44f7a8e17e74bde963cde}")
    private DemandConfigValueObject<?> configData;


    @Override
    public void checkAndSet() {
        if (Objects.isNull(configGroupId) && (Objects.isNull(instanceType) || CollectionUtils.isEmpty(instanceIds))) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_7e07fd2f37e34803856c83a771dbe045"));
        }
    }
}
