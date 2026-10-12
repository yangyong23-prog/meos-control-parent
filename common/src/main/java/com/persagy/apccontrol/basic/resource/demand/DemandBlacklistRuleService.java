package com.persagy.apccontrol.basic.resource.demand;

import com.persagy.apccontrol.basic.config.dao.BaseService;
import com.persagy.apccontrol.basic.model.entity.demand.DemandBlacklistRule;
import com.persagy.apccontrol.basic.model.param.demand.DemandBlacklistConfigAddParam;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * @author yuyao
 * @description 针对表【demand_blacklist_rule(需求黑名单规则表)】的数据库操作Service
 * @createDate 2025-03-04 11:19:33
 */
public interface DemandBlacklistRuleService extends BaseService<DemandBlacklistRule> {


    /**
     * 添加规则
     * 每次按照模版进行覆盖
     *
     * @param param
     */
    public void addRules(DemandBlacklistConfigAddParam param);

    @Nullable
    DemandBlacklistRule queryByDemandTemplateType(@NonNull DemandTemplateTypeEnum templateTypeEnum);
}
