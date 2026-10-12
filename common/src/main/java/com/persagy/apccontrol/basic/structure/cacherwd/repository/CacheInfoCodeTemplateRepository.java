package com.persagy.apccontrol.basic.structure.cacherwd.repository;

import com.persagy.apccontrol.basic.model.base.domain.Repository;
import com.persagy.apccontrol.basic.model.entity.cache.CacheInfoCodeTemplate;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.resource.cache.param.CacheInfoCodeTemplateQueryDaoParam;
import com.persagy.apccontrol.basic.resource.infocodeconfig.param.InfoCodeTemplateTypeQueryDaoParam;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 缓存-信息点模版仓储
 *
 * @author JiYinchuan
 * @since 7.260302.0
 */
public interface CacheInfoCodeTemplateRepository extends Repository {

    /**
     * 根据Key查询缓存
     *
     * @param key 模版唯一Key
     * @return 缓存信息点模版
     * @since 7.260302.0
     */
    Optional<CacheInfoCodeTemplate> getByKeyOpt(@NonNull String key);

    /**
     * 查询缓存列表
     *
     * @param queryDaoParam 查询参数
     * @return 缓存信息点模版列表
     * @since 7.260302.0
     */
    List<CacheInfoCodeTemplate> list(@NonNull CacheInfoCodeTemplateQueryDaoParam queryDaoParam);

    /**
     * 查询一级运行监测类型实际维护的规则缓存键。
     *
     * <p>一级类型只用于定位模板元数据；规则关联表自身只使用返回的缓存键限定范围。</p>
     *
     * @param runningMonitorType 一级运行监测类型
     * @return 规则缓存键列表
     * @since v7.260701.0
     */
    @NonNull List<String> listKeysByRunningMonitorType(
            @NonNull RunningMonitorTypeEnum runningMonitorType);

    /**
     * 添加缓存
     *
     * @param templateTypeId 模版类型ID
     * @since 7.260302.0
     */
    void add(@NonNull Long templateTypeId);

    /**
     * 删除缓存
     *
     * @param templateIds 模版ID列表
     * @since 7.260302.0
     */
    void del(@NonNull Collection<Long> templateIds);

    /**
     * 根据查询参数重建缓存
     *
     * <p>先完成目标模板对象及扩展数据准备，再替换模板元数据和对应缓存键；只有单一级类型
     * 完整范围允许清理目标外旧键，带模板或设备类筛选的窄范围不会扩大删除。</p>
     *
     * @param queryDaoParam 查询参数
     * @since 7.250401.0
     */
    void rebuildByParam(@NonNull InfoCodeTemplateTypeQueryDaoParam queryDaoParam);

}
