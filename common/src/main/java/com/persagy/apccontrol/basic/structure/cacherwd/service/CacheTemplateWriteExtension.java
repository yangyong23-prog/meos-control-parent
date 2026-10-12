package com.persagy.apccontrol.basic.structure.cacherwd.service;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeTemplateTypeModel;
import com.persagy.framework.common.exception.BusinessException;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.List;

/**
 * 模板缓存写入扩展。
 *
 * <p>模板重建服务根据一级类型只会选择新版对象模型或现有 RWD 缓存中的一组方法，不会在同次
 * 替换中同时准备两套持久化动作。所有远程计算必须在准备方法中完成，返回动作只执行本地 SQL。</p>
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public interface CacheTemplateWriteExtension {

    /**
     * 准备新版模板缓存保存动作。
     *
     * @param templateTypeModel 模板类型模型
     * @param objects            当前模板键的完整对象快照
     * @return 已完成准备、可在新版单 Key 事务中执行的动作
     * @throws BusinessException 计算或写入准备失败时抛出
     * @since v7.260701.0
     */
    @NonNull CacheWriteAction prepareModelSave(
            @NonNull InfoCodeTemplateTypeModel templateTypeModel,
            @NonNull List<DtpRwdObjectDTO> objects);

    /**
     * 准备新版模板缓存删除动作。
     *
     * @param templateKeys       待删除的模板键
     * @param runningMonitorType 模板所属一级类型
     * @return 已完成准备、可在新版单 Key 事务中执行的动作
     * @since v7.260701.0
     */
    @NonNull CacheWriteAction prepareModelRemove(
            @NonNull Collection<String> templateKeys,
            @NonNull RunningMonitorTypeEnum runningMonitorType);

    /**
     * 准备现有 RWD 模板缓存保存动作。
     *
     * <p>不维护现有扩展表的实现保持空动作，避免其他系统触碰新版缓存表。</p>
     *
     * @param templateTypeModel 模板类型模型
     * @param objects            当前模板键的完整对象快照
     * @return 已完成准备的现有缓存动作
     * @throws BusinessException 计算或写入准备失败时抛出
     * @since v7.260701.0
     */
    default @NonNull CacheWriteAction prepareRwdSave(
            @NonNull InfoCodeTemplateTypeModel templateTypeModel,
            @NonNull List<DtpRwdObjectDTO> objects) {
        return () -> {
        };
    }

    /**
     * 准备现有 RWD 模板缓存删除动作。
     *
     * @param templateKeys       待删除的模板键
     * @param runningMonitorType 模板所属一级类型
     * @return 已完成准备的现有缓存动作
     * @since v7.260701.0
     */
    default @NonNull CacheWriteAction prepareRwdRemove(
            @NonNull Collection<String> templateKeys,
            @NonNull RunningMonitorTypeEnum runningMonitorType) {
        return () -> {
        };
    }
}
