package com.persagy.apccontrol.basic.structure.cacherwd.repository;

import com.persagy.apccontrol.basic.model.base.domain.Repository;
import com.persagy.apccontrol.basic.model.entity.cache.CacheTemplateObjectDetail;
import com.persagy.apccontrol.basic.resource.cache.param.CacheTemplateObjectDetailBusinessKey;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.dto.InfoCodeRwdObjectDetailDTO;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 缓存模板对象明细仓储。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public interface CacheTemplateObjectDetailRepository extends Repository {

    /**
     * 批量查询业务键对应的模板对象明细。
     *
     * <p>写入阶段保证业务键和结果结构，读取阶段返回数据库中实际查询到的记录。</p>
     *
     * @param businessKeys 模板对象明细业务键集合
     * @return 业务键到有序对象明细列表的映射
     * @since v7.260701.0
     */
    @NonNull Map<CacheTemplateObjectDetailBusinessKey, List<InfoCodeRwdObjectDetailDTO>>
    mapResultsByBusinessKeys(@NonNull Collection<CacheTemplateObjectDetailBusinessKey> businessKeys);

    /**
     * 完整替换当前项目单个模板键的全部对象明细。
     *
     * @param templateKey 模板键
     * @param details     当前模板键的完整目标明细；空集合表示清空当前模板范围
     * @since v7.260701.0
     */
    void replaceDetails(
            @NonNull String templateKey,
            @NonNull Collection<CacheTemplateObjectDetail> details);
}
