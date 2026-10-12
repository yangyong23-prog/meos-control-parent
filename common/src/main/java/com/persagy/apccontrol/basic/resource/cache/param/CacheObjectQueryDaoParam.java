package com.persagy.apccontrol.basic.resource.cache.param;

import com.persagy.apccontrol.basic.model.base.domain.repository.BaseRepositoryQueryParam;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

/**
 * 缓存对象查询参数。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CacheObjectQueryDaoParam extends BaseRepositoryQueryParam<CacheObject> {

    /**
     * 对象 ID 集合。
     *
     * <p>显式空集合表示不查询对象，不能退化为查询当前项目全部对象。</p>
     *
     * @since v7.260701.0
     */
    private @Nullable Collection<String> ids;

    /**
     * 对象类编码集合。
     *
     * @since v7.260802.0
     */
    private @Nullable Collection<String> classCodes;

    /**
     * 监测监控状态筛选 code 集合。
     *
     * <p>多个 code 按命中任意一个处理；空集合表示不增加状态筛选条件。</p>
     *
     * @since v7.260701.0
     */
    private @Nullable Collection<String> monitoringSurveillanceStatusCodes;
}
