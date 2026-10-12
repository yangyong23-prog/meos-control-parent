package com.persagy.apccontrol.basic.service.demand.blacklist;

import com.persagy.apccontrol.basic.model.param.demand.DemandBlacklistConfigAddParam;
import com.persagy.apccontrol.basic.model.vo.demand.DemandBlacklistRuleVO;

import java.util.Set;

/**
 * 需求黑名单服务
 */
public interface DemandBlacklistService {


    /**
     * 保存黑名单规则
     */
    void addRules(DemandBlacklistConfigAddParam param);


    /**
     * 新增根据规则查询黑名单空间ID
     */
    Set<String> listSpaceIdsByRule(DemandBlacklistConfigAddParam param);


    DemandBlacklistRuleVO query(DemandBlacklistConfigAddParam param);
}
