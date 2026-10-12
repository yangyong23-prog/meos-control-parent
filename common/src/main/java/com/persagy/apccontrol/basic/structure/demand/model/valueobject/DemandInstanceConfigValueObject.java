package com.persagy.apccontrol.basic.structure.demand.model.valueobject;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.BooleanUtils;

/**
 * 需求实例配置值对象
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@Accessors(chain = true)
public class DemandInstanceConfigValueObject {

    /**
     * 是否被屏蔽, 仅空间可配置
     */
    private Boolean shielded;

    /**
     * 是否在黑名单中, 仅空间可配置
     */
    private Boolean inBlacklisted;

    /**
     * 是否为有效配置
     *
     * @return 是否为有效配置
     * @since 5.241102.0
     */
    public boolean valid() {
        return BooleanUtils.isTrue(shielded) || BooleanUtils.isTrue(inBlacklisted);
    }

    /**
     * 创建当前实例
     *
     * @return 当前实例
     * @since v6.0.0
     */
    public static DemandInstanceConfigValueObject newInstance() {
        return new DemandInstanceConfigValueObject().setShielded(false).setInBlacklisted(false);
    }
}
