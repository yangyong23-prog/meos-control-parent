package com.persagy.apccontrol.basic.resource.cache.param;

import com.persagy.apccontrol.basic.model.base.domain.repository.BaseRepositoryQueryParam;
import com.persagy.apccontrol.basic.model.entity.cache.CacheTemplateObjectDetail;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

/**
 * 缓存模板对象明细查询参数。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CacheTemplateObjectDetailQueryDaoParam
        extends BaseRepositoryQueryParam<CacheTemplateObjectDetail> {

    /**
     * 待精确匹配的完整业务键集合。
     *
     * <p>为 {@code null} 或空集合时不得退化为查询当前项目全部明细。</p>
     *
     * @since v7.260701.0
     */
    private @Nullable Collection<CacheTemplateObjectDetailBusinessKey> businessKeys;
}
