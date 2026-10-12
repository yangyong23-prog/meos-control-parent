package com.persagy.apccontrol.basic.structure.cacherwd.repository;

import com.persagy.apccontrol.basic.model.entity.cache.CacheTemplateObjectDetail;
import com.persagy.apccontrol.basic.resource.cache.CacheTemplateObjectDetailService;
import com.persagy.apccontrol.basic.resource.cache.param.CacheTemplateObjectDetailBusinessKey;
import com.persagy.apccontrol.basic.resource.cache.param.CacheTemplateObjectDetailQueryDaoParam;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheTemplateObjectDetailRecordConverter;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.dto.InfoCodeRwdObjectDetailDTO;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 缓存模板对象明细仓储实现。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Repository
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CacheTemplateObjectDetailRepositoryImpl implements CacheTemplateObjectDetailRepository {

    /**
     * 缓存模板对象明细资源服务。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheTemplateObjectDetailService cacheTemplateObjectDetailService;

    /**
     * 缓存模板对象明细记录转换器。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheTemplateObjectDetailRecordConverter detailRecordConverter;

    @Override
    public @NonNull Map<CacheTemplateObjectDetailBusinessKey, List<InfoCodeRwdObjectDetailDTO>>
    mapResultsByBusinessKeys(
            @NonNull Collection<CacheTemplateObjectDetailBusinessKey> businessKeys) {
        CacheTemplateObjectDetailQueryDaoParam queryParam = new CacheTemplateObjectDetailQueryDaoParam();
        queryParam.setBusinessKeys(businessKeys);
        queryParam.addColumns(
                CacheTemplateObjectDetail::getTemplateKey,
                CacheTemplateObjectDetail::getObjectId,
                CacheTemplateObjectDetail::getInfoCode,
                CacheTemplateObjectDetail::getResultJson);
        List<CacheTemplateObjectDetail> details =
                this.cacheTemplateObjectDetailService.listByBusinessKeys(queryParam);
        Map<CacheTemplateObjectDetailBusinessKey, List<InfoCodeRwdObjectDetailDTO>> result =
                new LinkedHashMap<>();
        for (CacheTemplateObjectDetail detail : details) {
            List<InfoCodeRwdObjectDetailDTO> convertedResult =
                    this.detailRecordConverter.toResult(detail);
            if (convertedResult != null) {
                // 读取只返回实际可恢复的记录；历史坏 JSON 由 fail-safe 转换记录日志后忽略。
                result.put(CacheTemplateObjectDetailBusinessKey.of(detail), convertedResult);
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceDetails(
            @NonNull String templateKey,
            @NonNull Collection<CacheTemplateObjectDetail> details) {
        this.cacheTemplateObjectDetailService.removeByTemplateKey(templateKey);
        this.cacheTemplateObjectDetailService.insertDetails(details);
    }

    @Override
    public @NonNull String logTag() {
        return "CacheTemplateObjectDetailRepositoryImpl";
    }
}
