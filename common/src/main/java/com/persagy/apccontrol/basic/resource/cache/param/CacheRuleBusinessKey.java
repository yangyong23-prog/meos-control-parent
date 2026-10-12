package com.persagy.apccontrol.basic.resource.cache.param;

import com.persagy.apccontrol.basic.model.entity.cache.CacheRule;
import com.persagy.apccontrol.basic.structure.cache.enumeration.CacheIotRuleTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * 缓存规则业务键。
 *
 * <p>项目 ID 由当前项目上下文提供，业务键对象只携带其余组成字段：对象 ID、信息点编码
 * 和云边规则类型。该键用于批量匹配已有规则并复用稳定主键。</p>
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class CacheRuleBusinessKey {

    /**
     * 项目 ID。
     *
     * @since v7.260701.0
     */
    private @Nullable String projectId;

    /**
     * 规则所属对象 ID。
     *
     * @since v7.260701.0
     */
    private @Nullable String objectId;

    /**
     * 规则信息点编码。
     *
     * @since v7.260701.0
     */
    private @Nullable String infoCode;

    /**
     * 云端或边缘端规则类型。
     *
     * @since v7.260701.0
     */
    private @Nullable CacheIotRuleTypeEnum type;

    /**
     * 从规则实体创建业务键。
     *
     * @param rule 规则实体
     * @return 规则业务键
     * @since v7.260701.0
     */
    public static @NonNull CacheRuleBusinessKey of(@NonNull CacheRule rule) {
        return new CacheRuleBusinessKey(
                rule.getProjectId(), rule.getObjectId(), rule.getInfoCode(), rule.getType());
    }
}
