package com.persagy.apccontrol.basic.resource.cache;

import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectRel;
import com.persagy.apccontrol.basic.resource.cache.param.CacheObjectRelReplaceScope;
import com.persagy.apccontrol.basic.resource.cache.param.CacheObjectRelQueryDaoParam;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.List;

/**
 * 缓存对象关系资源服务。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public interface CacheObjectRelService {

    /**
     * 按关系主体对象 ID 正向查询当前项目关系。
     *
     * @param param 查询参数
     * @return 正向关系列表
     * @since v7.260701.0
     */
    @NonNull List<CacheObjectRel> listForward(@NonNull CacheObjectRelQueryDaoParam param);

    /**
     * 按关系目标对象 ID 反向查询当前项目关系。
     *
     * @param param 查询参数
     * @return 反向命中的原始方向关系列表
     * @since v7.260701.0
     */
    @NonNull List<CacheObjectRel> listReverse(@NonNull CacheObjectRelQueryDaoParam param);

    /**
     * 批量新增当前项目的对象关系。
     *
     * @param relations 待新增关系
     * @since v7.260701.0
     */
    void addRelations(@NonNull Collection<CacheObjectRel> relations);

    /**
     * 批量完整替换当前项目的关系范围。
     *
     * @param scopes 关系完整替换范围
     * @since v7.260701.0
     */
    void replaceRelations(@NonNull Collection<CacheObjectRelReplaceScope> scopes);

    /**
     * 按关系主体和关系类型删除当前项目的正向关系。
     *
     * @param objectIds 主体对象 ID
     * @param graphCode 图谱编码
     * @param relCode 关系编码
     * @since v7.260701.0
     */
    void removeForwardRelations(
            @NonNull Collection<String> objectIds,
            @NonNull String graphCode,
            @NonNull String relCode);

    /**
     * 按关系目标和关系类型删除当前项目的反向命中关系。
     *
     * @param relObjectIds 目标对象 ID
     * @param graphCode 图谱编码
     * @param relCode 关系编码
     * @since v7.260701.0
     */
    void removeReverseRelations(
            @NonNull Collection<String> relObjectIds,
            @NonNull String graphCode,
            @NonNull String relCode);

}
