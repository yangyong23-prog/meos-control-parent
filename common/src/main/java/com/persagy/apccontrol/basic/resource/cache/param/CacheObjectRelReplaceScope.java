package com.persagy.apccontrol.basic.resource.cache.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;

import java.util.Collection;

/**
 * 缓存对象关系的完整替换范围。
 *
 * <p>一个范围由主体、图谱和关系编码唯一确定；目标集合为空时表示清理该范围。资源层会在
 * 同一事务内批量删除全部范围，再分片插入非空目标，避免按范围逐条执行 SQL。</p>
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CacheObjectRelReplaceScope {

    /**
     * 关系主体对象 ID。
     *
     * @since v7.260701.0
     */
    private @NonNull String objectId;

    /**
     * 图谱编码。
     *
     * @since v7.260701.0
     */
    private @NonNull String graphCode;

    /**
     * 关系编码。
     *
     * @since v7.260701.0
     */
    private @NonNull String relCode;

    /**
     * 完整目标对象 ID 集合；空集合表示清理范围。
     *
     * @since v7.260701.0
     */
    private @NonNull Collection<String> relObjectIds;
}
