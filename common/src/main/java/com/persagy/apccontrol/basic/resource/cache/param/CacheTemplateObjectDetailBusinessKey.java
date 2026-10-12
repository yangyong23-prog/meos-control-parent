package com.persagy.apccontrol.basic.resource.cache.param;

import com.persagy.apccontrol.basic.model.entity.cache.CacheTemplateObjectDetail;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * 缓存模板对象明细完整业务键。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class CacheTemplateObjectDetailBusinessKey {

    /**
     * 模板键。
     *
     * @since v7.260701.0
     */
    private @Nullable String templateKey;

    /**
     * 当前信息点聚合的根对象 ID。
     *
     * @since v7.260701.0
     */
    private @Nullable String objectId;

    /**
     * 信息点编码。
     *
     * @since v7.260701.0
     */
    private @Nullable String infoCode;

    /**
     * 从模板对象明细实体创建完整业务键。
     *
     * @param detail 模板对象明细实体
     * @return 完整业务键
     * @since v7.260701.0
     */
    public static @NonNull CacheTemplateObjectDetailBusinessKey of(
            @NonNull CacheTemplateObjectDetail detail) {
        CacheTemplateObjectDetailBusinessKey businessKey =
                new CacheTemplateObjectDetailBusinessKey();
        businessKey.setTemplateKey(detail.getTemplateKey());
        businessKey.setObjectId(detail.getObjectId());
        businessKey.setInfoCode(detail.getInfoCode());
        return businessKey;
    }
}
