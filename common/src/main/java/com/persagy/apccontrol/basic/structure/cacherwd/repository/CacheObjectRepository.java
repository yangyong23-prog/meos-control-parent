package com.persagy.apccontrol.basic.structure.cacherwd.repository;

import com.persagy.apccontrol.basic.model.base.domain.Repository;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObject;
import com.persagy.apccontrol.basic.resource.cache.param.CacheObjectQueryDaoParam;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 新缓存对象与缓存键关联仓储。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public interface CacheObjectRepository extends Repository {

    /**
     * 按缓存键和调用方字段批量读取对象，目标类型默认为 {@link DtpRwdObjectDTO}。
     *
     * @param cacheKey    缓存键
     * @param withColumns 本次调用需要返回的 RWD 字段；{@code null} 或空集合表示查询全字段
     * @return 实际查询到的对象列表
     * @since v7.260701.0
     */
    @NonNull List<DtpRwdObjectDTO> listByCacheKey(
            @NonNull String cacheKey,
            @Nullable Collection<String> withColumns);

    /**
     * 按缓存键和调用方字段批量读取指定 RWD DTO 类型。
     *
     * @param cacheKey    缓存键
     * @param targetClass 目标 DTO 类型
     * @param withColumns 本次调用需要返回的 RWD 字段；{@code null} 或空集合表示查询全字段
     * @param <T>         RWD DTO 类型
     * @return 实际查询到的对象列表
     * @since v7.260701.0
     */
    <T extends DtpRwdObjectDTO> @NonNull List<T> listByCacheKey(
            @NonNull String cacheKey,
            @NonNull Class<T> targetClass,
            @Nullable Collection<String> withColumns);

    /**
     * 按对象类编码和调用方字段批量读取当前项目对象。
     *
     * @param classCode 对象类编码
     * @param targetClass 目标 DTO 类型
     * @param withColumns 调用方字段
     * @param <T> RWD DTO 类型
     * @return 当前项目该类编码对象
     * @since v7.260802.0
     */
    <T extends DtpRwdObjectDTO> @NonNull List<T> listByClassCode(
            @NonNull String classCode,
            @NonNull Class<T> targetClass,
            @Nullable Collection<String> withColumns);

    /**
     * 按对象 ID 和调用方字段批量读取对象，目标类型默认为 {@link DtpRwdObjectDTO}。
     *
     * @param objectIds   对象 ID 集合
     * @param withColumns 本次调用需要返回的 RWD 字段；{@code null} 或空集合表示查询全字段
     * @return 实际查询到的对象列表
     * @since v7.260701.0
     */
    @NonNull List<DtpRwdObjectDTO> listByObjectIds(
            @NonNull Collection<String> objectIds,
            @Nullable Collection<String> withColumns);

    /**
     * 按对象 ID 和调用方字段批量读取指定 RWD DTO 类型。
     *
     * @param objectIds   对象 ID 集合
     * @param targetClass 目标 DTO 类型
     * @param withColumns 本次调用需要返回的 RWD 字段；{@code null} 或空集合表示查询全字段
     * @param <T>         RWD DTO 类型
     * @return 实际查询到的对象列表
     * @since v7.260701.0
     */
    <T extends DtpRwdObjectDTO> @NonNull List<T> listByObjectIds(
            @NonNull Collection<String> objectIds,
            @NonNull Class<T> targetClass,
            @Nullable Collection<String> withColumns);

    /**
     * 按单个缓存键查询当前项目的对象 ID。
     *
     * @param cacheKey 缓存键
     * @return 对象 ID 列表；没有关联对象时返回空列表，保留关联查询的返回顺序和重复项
     * @since v7.260701.0
     */
    @NonNull List<String> listObjectIdsByCacheKey(@NonNull String cacheKey);

    /**
     * 查询当前项目的缓存对象。
     *
     * @param param 查询参数
     * @return 缓存对象列表
     * @since v7.260701.0
     */
    @NonNull List<CacheObject> listObjects(@NonNull CacheObjectQueryDaoParam param);

    /**
     * 按多个缓存键和调用方字段批量读取对象，目标类型默认为 {@link DtpRwdObjectDTO}。
     *
     * @param cacheKeys   缓存键集合
     * @param withColumns 本次调用需要返回的 RWD 字段；{@code null} 或空集合表示查询全字段
     * @return 缓存键到实际查询到的对象列表映射
     * @since v7.260701.0
     */
    @NonNull Map<String, List<DtpRwdObjectDTO>> mapByCacheKeys(
            @NonNull Collection<String> cacheKeys,
            @Nullable Collection<String> withColumns);

    /**
     * 按多个缓存键和调用方字段批量读取指定 RWD DTO 类型。
     *
     * <p>字段名使用 RWD 字段名；公共字段从独立列读取，请求任一已配置扩展字段时读取对象 JSON，
     * 最终 DTO 仍只保留本次请求字段。{@code withColumns} 为 {@code null} 或空集合时查询全字段。</p>
     *
     * @param cacheKeys   缓存键集合
     * @param targetClass 目标 DTO 类型
     * @param withColumns 本次调用需要返回的 RWD 字段；{@code null} 或空集合表示查询全字段
     * @param <T>         RWD DTO 类型
     * @return 缓存键到实际查询到的对象列表映射
     * @since v7.260701.0
     */
    <T extends DtpRwdObjectDTO> @NonNull Map<String, List<T>> mapByCacheKeys(
            @NonNull Collection<String> cacheKeys,
            @NonNull Class<T> targetClass,
            @Nullable Collection<String> withColumns);

    /**
     * 按缓存键批量查询当前项目的对象 ID。
     *
     * @param cacheKeys 缓存键集合
     * @return 缓存键到对象 ID 列表的映射；结果包含每个请求缓存键，value 始终非
     * {@code null}，没有关联对象时为空列表，重复关联自动去重
     * @since v7.260701.0
     */
    @NonNull Map<String, List<String>> mapObjectIdsByCacheKeys(@NonNull Collection<String> cacheKeys);

    /**
     * 按对象 ID 去重后批量新增或更新缓存对象。
     *
     * @param objects 缓存对象集合
     * @since v7.260701.0
     */
    void upsertObjects(@NonNull Collection<CacheObject> objects);

    /**
     * 删除当前项目指定对象类编码的完整对象快照。
     *
     * @param classCodes 对象类编码集合
     * @since v7.260802.0
     */
    void removeObjectsByClassCodes(@NonNull Collection<String> classCodes);

    /**
     * 完整替换当前项目单个缓存键的对象 ID 集合。
     *
     * @param cacheKey  缓存键
     * @param objectIds 完整目标对象 ID 集合；空集合表示清空当前缓存键
     * @since v7.260701.0
     */
    void replaceObjectIds(@NonNull String cacheKey, @NonNull Collection<String> objectIds);

    /**
     * 删除当前项目指定缓存键的对象关联。
     *
     * @param cacheKeys 缓存键集合
     * @since v7.260701.0
     */
    void removeObjectKeyRelations(@NonNull Collection<String> cacheKeys);

}
