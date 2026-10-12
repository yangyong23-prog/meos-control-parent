package com.persagy.apccontrol.basic.structure.demand.gateway.param;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.BaseDemandConfig;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Set;

/**
 * 需求配置新增参数
 *
 * @param <T> 配置类型
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Data
public class DemandConfigSaveOrUpdateParam<T extends BaseDemandConfig> implements BaseCheckParamProvider {

    /**
     * 模版ID
     */
    @NotNull(message = "{key_02bebf76336942da9503b791f451570d}")
    private Long templateId;

    /**
     * 场景ID
     */
    private Long sceneId;

    /**
     * 配置组ID
     */
    private Long configGroupId;

    /**
     * 配置组名称
     */
    private String configGroupName;

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
    @Valid
    @NotNull(message = "{key_52056cc794e44f7a8e17e74bde963cde}")
    private DemandConfigValueObject<T> configData;

    /**
     * 是否为新增空间功能类型
     */
    private Boolean addRoomFuncType;

    @Deprecated
    private Boolean deleteFlag = false;

    @Override
    public void checkAndSet() {
        if (Objects.isNull(configGroupId) && (Objects.isNull(instanceType) || CollectionUtils.isEmpty(instanceIds))) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_7e07fd2f37e34803856c83a771dbe045"));
        }
        configData.checkAndSet();
    }
}
