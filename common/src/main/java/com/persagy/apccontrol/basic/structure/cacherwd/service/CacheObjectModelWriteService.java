package com.persagy.apccontrol.basic.structure.cacherwd.service;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.framework.common.exception.BusinessException;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.Map;
import java.util.function.BooleanSupplier;

/**
 * 云端缓存对象模型写入编排。
 *
 * <p>该服务只投影并提交调用方提供的完整对象快照和本地动作，不查询 RWD。
 * 通用对象关系通过独立入口提交，调用方明确决定是否维护该关系范围。</p>
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public interface CacheObjectModelWriteService {

    /**
     * 使用调用方准备的完整对象快照替换新版对象模型缓存。
     *
     * @param modelCacheKey 新版对象模型缓存 Key
     * @param sourceObjects 当前缓存 Key 的完整 RWD 对象快照
     * @return 是否替换成功
     * @throws BusinessException 本地记录校验或写入失败时抛出
     * @since v7.260701.0
     */
    boolean replaceModelCacheKey(
            @NonNull String modelCacheKey,
            @NonNull Collection<? extends DtpRwdObjectDTO> sourceObjects);

    /**
     * 使用调用方准备的完整对象快照和扩展动作替换新版对象模型缓存。
     *
     * <p>对象快照在事务开始前投影一次；对象、模型视图、Key 关联和扩展动作
     * 在同一个短事务中按参数顺序提交。该入口不维护通用对象关系。</p>
     *
     * @param modelCacheKey      新版对象模型缓存 Key
     * @param sourceObjects 当前缓存 Key 的完整 RWD 对象快照
     * @param writeActions 已完成远程准备的有序扩展写入动作
     * @return 是否替换成功
     * @throws BusinessException 本地记录校验或扩展写入失败时抛出
     * @since v7.260701.0
     */
    boolean replaceModelCacheKey(
            @NonNull String modelCacheKey,
            @NonNull Collection<? extends DtpRwdObjectDTO> sourceObjects,
            @NonNull Collection<CacheWriteAction> writeActions);

    /**
     * 使用调用方准备的完整对象快照替换新版对象模型，并执行替换后动作。
     *
     * <p>{@code afterReplaceAction} 只能执行本地持久化；返回 {@code false} 时整个替换事务
     * 回滚，以保留替换前的完整 Key。</p>
     *
     * @param modelCacheKey 新版对象模型缓存 Key
     * @param sourceObjects 当前缓存 Key 的完整 RWD 对象快照
     * @param writeActions 已完成远程准备的有序扩展写入动作
     * @param afterReplaceAction 缓存 Key 替换后的本地提交动作
     * @return 是否完成原子替换和提交动作
     * @throws BusinessException 本地记录校验或写入失败时抛出
     * @since v7.260701.0
     */
    boolean replaceModelCacheKey(
            @NonNull String modelCacheKey,
            @NonNull Collection<? extends DtpRwdObjectDTO> sourceObjects,
            @NonNull Collection<CacheWriteAction> writeActions,
            @NonNull BooleanSupplier afterReplaceAction);

    /**
     * 原子替换多个新版对象模型缓存 Key 及其本地扩展范围。
     *
     * <p>所有 Key 的源对象必须在事务开始前完成投影；同一短事务内按 Map 顺序写入对象、
     * 模型视图和 Key 关联，再执行已经准备好的关系等本地动作。任一步异常都会回滚全部
     * Key 和动作，避免 READY 项目暴露半新半旧数据。</p>
     *
     * @param sourceObjectsByKey 缓存 Key 到当前完整 RWD 对象快照，Map 顺序即替换顺序
     * @param writeActions 已在事务外准备完成的有序本地动作
     * @return 是否完整提交所有 Key 和动作
     * @throws BusinessException 任一快照投影、校验或本地写入失败时抛出
     * @since v7.260802.0
     */
    boolean replaceModelCacheKeys(
            @NonNull Map<String, ? extends Collection<? extends DtpRwdObjectDTO>>
                    sourceObjectsByKey,
            @NonNull Collection<CacheWriteAction> writeActions);

    /**
     * 在事务编排层一次提交多个缓存 Key 的完整对象快照和通用关系。
     *
     * <p>事务外准备全部 Key 的对象投影和关系范围转换，事务内按 Key 逐组 UPSERT 对象、
     * 替换键关联、替换关系、执行扩展动作。任一阶段失败或 {@code afterReplaceAction}
     * 返回 {@code false} 时整体回滚，实现同类型所有缓存写入的物理原子性。
     * RWD 查询和异步准备必须在事务外完成。</p>
     *
     * @param sourceObjectsByKey  缓存 Key 到完整对象快照的映射
     * @param writeActions        已在事务外准备完成的有序本地动作
     * @param relationsByType     关系类型到该类型完整关系对象快照的映射
     * @param afterReplaceAction  全部 Key 和关系替换后的本地提交动作，返回 {@code false} 时整体回滚
     * @return 是否完整提交所有 Key、关系和动作
     * @throws BusinessException 关联 Key 为空、快照投影或写入失败时抛出
     * @since v7.260802.0
     */
    boolean replaceModelCacheKeysAndRelations(
            @NonNull Map<String, ? extends Collection<? extends DtpRwdObjectDTO>>
                    sourceObjectsByKey,
            @NonNull Collection<CacheWriteAction> writeActions,
            @NonNull Map<DtpRwdRelationTypeEnum, ? extends Collection<? extends DtpRwdObjectDTO>>
                    relationsByType,
            @NonNull BooleanSupplier afterReplaceAction);

    /**
     * 在独立短事务中提交调用方已经准备好的通用对象关系完整快照。
     *
     * <p>每个对象的 {@code id} 是关系主体，{@code relObjIds} 是该主体在指定关系类型下的
     * 完整目标集合。目标为 {@code null} 或空集合仍表示明确清理该主体的关系范围。
     * 该入口不查询 RWD，也不根据对象或缓存 Key 推断关系。</p>
     *
     * @param relationType 关系类型
     * @param relationObjects 待完整替换的关系对象快照
     * @throws BusinessException 关系范围校验或写入失败时抛出
     * @since v7.260701.0
     */
    void replaceObjectRelations(
            @NonNull DtpRwdRelationTypeEnum relationType,
            @NonNull Collection<? extends DtpRwdObjectDTO> relationObjects);

    /**
     * 将已经准备好的通用对象关系完整快照转换为本地写入动作。
     *
     * <p>关系 DTO 在返回动作前完成校验和替换范围转换；动作执行时只访问本地关系仓储，
     * 可与多 Key 原子替换共用同一事务，不能再次开启独立事务。</p>
     *
     * @param relationType 关系类型
     * @param relationObjects 待完整替换的关系对象快照
     * @return 已完成校验和转换的本地关系写入动作
     * @throws BusinessException 关系范围非法时抛出
     * @since v7.260802.0
     */
    @NonNull CacheWriteAction prepareObjectRelationsReplaceAction(
            @NonNull DtpRwdRelationTypeEnum relationType,
            @NonNull Collection<? extends DtpRwdObjectDTO> relationObjects);

    /**
     * 完整替换无缓存 Key 的公共对象类。
     *
     * <p>先在事务外投影完整快照，再在独立短事务中删除当前项目这些 classCode 的旧对象和
     * 模型视图并写入新快照。空快照仍会清理旧数据。</p>
     *
     * @param classCodes 要完整替换的对象类编码
     * @param sourceObjects 当前完整 RWD 对象快照
     * @return 是否完成替换
     * @since v7.260802.0
     */
    boolean replaceObjectClassCodes(
            @NonNull Collection<String> classCodes,
            @NonNull Collection<? extends DtpRwdObjectDTO> sourceObjects);

    /**
     * 仅删除新版对象模型的缓存 Key 关联。
     *
     * <p>该入口供新版白名单切换全量或删除配置使用，不删除旧对象缓存，也不扫描对象、
     * 模型视图或无 Key 所有权的关系主表。</p>
     *
     * @param cacheKeys 新版缓存 Key 集合
     * @since v7.260701.0
     */
    void removeCacheKeys(@NonNull Collection<String> cacheKeys);

    /**
     * 仅删除新版对象模型的缓存 Key 关联，并执行有明确业务所有权的新版扩展动作。
     *
     * @param cacheKeys   新版缓存 Key 集合
     * @param writeActions 已准备的新版明细、规则或关系删除动作
     * @throws BusinessException 扩展写入失败时抛出
     * @since v7.260701.0
     */
    void removeCacheKeys(
            @NonNull Collection<String> cacheKeys,
            @NonNull Collection<CacheWriteAction> writeActions);

    /**
     * 在同一短事务中先执行本地持久化动作，再删除新版对象模型缓存 Key 关联。
     *
     * <p>该入口仅适用于不包含远程调用的本地写入。{@code beforeRemoveAction} 返回
     * {@code false} 时整个事务回滚，缓存 Key 保持不变。</p>
     *
     * @param cacheKeys 新版缓存 Key 集合
     * @param beforeRemoveAction 删除 Key 前的本地持久化动作
     * @return 是否完成原子持久化和 Key 删除
     * @throws BusinessException 本地写入失败时抛出
     * @since v7.260701.0
     */
    boolean removeCacheKeys(
            @NonNull Collection<String> cacheKeys,
            @NonNull BooleanSupplier beforeRemoveAction);
}
