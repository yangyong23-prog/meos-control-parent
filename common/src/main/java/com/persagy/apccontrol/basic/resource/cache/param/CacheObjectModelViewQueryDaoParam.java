package com.persagy.apccontrol.basic.resource.cache.param;

import com.persagy.apccontrol.basic.model.base.domain.repository.BaseRepositoryQueryParam;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectModelView;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

/**
 * 缓存对象模型视图查询参数。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CacheObjectModelViewQueryDaoParam extends BaseRepositoryQueryParam<CacheObjectModelView> {

    /**
     * 对象 ID 集合。
     *
     * <p>显式空集合表示不查询模型视图，不能退化为查询当前项目全部记录。</p>
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
     * 建筑 ID 集合。
     *
     * @since v7.260701.0
     */
    private @Nullable Collection<String> buildingIds;

    /**
     * 楼层 ID 集合。
     *
     * @since v7.260701.0
     */
    private @Nullable Collection<String> floorIds;

    /**
     * 物理世界对象类型。
     *
     * <p>模型图消费者可用该字段区分设备和空间，避免查询后再加载并反序列化无关视图 JSON。</p>
     *
     * @since v7.260701.0
     */
    private @Nullable String objectType;
}
