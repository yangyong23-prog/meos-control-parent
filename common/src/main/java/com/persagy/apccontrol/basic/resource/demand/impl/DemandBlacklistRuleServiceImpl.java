package com.persagy.apccontrol.basic.resource.demand.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.mapper.demand.DemandBlacklistRuleMapper;
import com.persagy.apccontrol.basic.model.entity.demand.DemandBlacklistRule;
import com.persagy.apccontrol.basic.model.param.demand.DemandBlacklistConfigAddParam;
import com.persagy.apccontrol.basic.resource.demand.DemandBlacklistRuleService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.database.service.SuperServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author yuyao
 * @description 针对表【demand_blacklist_rule(需求黑名单规则表)】的数据库操作Service实现
 * @createDate 2025-03-04 11:19:33
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandBlacklistRuleServiceImpl extends SuperServiceImpl<DemandBlacklistRuleMapper, DemandBlacklistRule>
        implements DemandBlacklistRuleService {

    private final ObjectMapper objectMapper;

    @Transactional
    @Override
    public void addRules(DemandBlacklistConfigAddParam param) {
        //删除当前模版下的配置
        this.lambdaUpdate()
                .eq(DemandBlacklistRule::getProjectId, ContextUtil.getProjectId())
                .eq(DemandBlacklistRule::getDemandType, param.getDemandTemplateType().toDemandTypeEnum())
                .eq(DemandBlacklistRule::getDemandTemplateType, param.getDemandTemplateType())
                .remove();
        //添加配置
        DemandBlacklistRule rule = new DemandBlacklistRule();
        rule.setProjectId(ContextUtil.getProjectId());
        rule.setDemandType(param.getDemandTemplateType().toDemandTypeEnum());
        rule.setDemandTemplateType(param.getDemandTemplateType());
        rule.setConfigData(param.convert());
        this.save(rule);
    }

    @Override
    public DemandBlacklistRule queryByDemandTemplateType(@NonNull DemandTemplateTypeEnum templateTypeEnum) {
        List<DemandBlacklistRule> list = this.lambdaQuery()
                .eq(DemandBlacklistRule::getProjectId, ContextUtil.getProjectId())
                .eq(DemandBlacklistRule::getDemandType, templateTypeEnum.toDemandTypeEnum())
                .eq(DemandBlacklistRule::getDemandTemplateType, templateTypeEnum)
                .list();
        return CollectionUtils.isEmpty(list) ? null : list.get(0);
    }
}




