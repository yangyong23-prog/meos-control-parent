package com.persagy.apccontrol.basic.structure.cacherwd.strategy.impl;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.resource.infocodeconfig.param.InfoCodeTemplateTypeQueryDaoParam;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheInfoCodeTemplateRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.strategy.BaseCacheRwdStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 物理世界对象缓存-信息点配置策略
 *
 * <p>------------------------- 公共方法 -------------------------</p>
 * <ul>
 *     <li><b>buildCacheData</b> - 构建缓存数据，通过 InfoCodeTemplateTypeModel.allowCache() 判断是否需要缓存</li>
 * </ul>
 *
 * @author JiYinchuan
 * @since 7.260401.0
 */
@Slf4j
@Component(CacheRwdInfoCodeConfigStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CacheRwdInfoCodeConfigStrategy extends BaseCacheRwdStrategy {

    public static final String BEAN_NAME = "cacheRwdInfoCodeConfigStrategy";

    private final CacheInfoCodeTemplateRepository cacheInfoCodeTemplateRepository;

    /**
     * 通用策略返回 null，表示匹配所有类型
     *
     * @return null
     */
    @Override
    public RunningMonitorTypeEnum runningMonitorType() {
        return null;
    }

    @Override
    public boolean buildCacheData(RunningMonitorTypeEnum runningMonitorType) {
        InfoCodeTemplateTypeQueryDaoParam queryDaoParam = new InfoCodeTemplateTypeQueryDaoParam();
        queryDaoParam.setRunningMonitorType(runningMonitorType);
        // 通过 InfoCodeTemplateTypeModel.allowCache() 在 rebuildByParam 中自动过滤
        this.cacheInfoCodeTemplateRepository.rebuildByParam(queryDaoParam);
        return true;
    }

    @Override
    public String logTag() {
        return "物理世界缓存-信息点配置策略";
    }
}