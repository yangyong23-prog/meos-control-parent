package com.persagy.apccontrol.basic.resource.cache.param;

import com.persagy.apccontrol.basic.model.base.domain.repository.BaseRepositoryQueryParam;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectRel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

/**
 * 缓存对象关系查询参数。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CacheObjectRelQueryDaoParam extends BaseRepositoryQueryParam<CacheObjectRel> {

    /**
     * 正向查询的关系主体对象 ID 集合。
     *
     * <p>对应 RWD 物理关系的 {@code objFrom/fromId}；为 {@code null} 或空集合时，
     * 正向查询直接返回空结果，不表示取消主体对象范围限制。</p>
     *
     * @since v7.260701.0
     */
    private @Nullable Collection<String> objectIds;

    /**
     * 反向查询的关系目标对象 ID 集合。
     *
     * <p>对应 RWD 物理关系的 {@code objTo/toId}；为 {@code null} 或空集合时，
     * 反向查询直接返回空结果，不表示取消目标对象范围限制。</p>
     *
     * @since v7.260701.0
     */
    private @Nullable Collection<String> relObjectIds;

    /**
     * 图谱编码。
     *
     * @since v7.260701.0
     */
    private @Nullable String graphCode;

    /**
     * 关系编码。
     *
     * @since v7.260701.0
     */
    private @Nullable String relCode;
}
