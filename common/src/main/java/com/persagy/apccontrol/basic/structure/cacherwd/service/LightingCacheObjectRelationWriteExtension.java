package com.persagy.apccontrol.basic.structure.cacherwd.service;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeTemplateTypeModel;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * 照明关系缓存写入扩展。
 *
 * <p>模板增量保存和模板删除均通过对象模型复合事务执行；初始化策略复用同一个关系服务，
 * 因此四类照明关系不会因入口不同而产生方向或字段差异。</p>
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class LightingCacheObjectRelationWriteExtension implements CacheTemplateWriteExtension {

    /**
     * 照明关系快照与本地写入服务。
     *
     * @since v7.260701.0
     */
    private final @NonNull LightingCacheObjectRelationService relationService;

    @Override
    public @NonNull CacheWriteAction prepareModelSave(
            @NonNull InfoCodeTemplateTypeModel templateTypeModel,
            @NonNull List<DtpRwdObjectDTO> objects) {
        if (templateTypeModel.getRunningMonitorType() != RunningMonitorTypeEnum.LIGHTING) {
            return () -> {
            };
        }
        return this.relationService.prepareSaveAction(
                templateTypeModel.getTemplateTypeKey(), objects,
                templateTypeModel.getRunningMonitorSecondType());
    }

    @Override
    public @NonNull CacheWriteAction prepareModelRemove(
            @NonNull Collection<String> templateKeys,
            @NonNull RunningMonitorTypeEnum runningMonitorType) {
        if (runningMonitorType != RunningMonitorTypeEnum.LIGHTING) {
            return () -> {
            };
        }
        return this.relationService.prepareRemoveAction(templateKeys);
    }
}
