package com.persagy.apccontrol.basic.structure.bizcheck.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.service.BizComponent;
import com.persagy.apccontrol.basic.structure.bizcheck.model.entity.BizCheckConfig;
import com.persagy.apccontrol.basic.structure.bizcheck.repository.mapper.BizCheckConfigMapper;
import com.persagy.framework.common.context.ContextUtil;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *
 * @description:
 * @author: wxf
 * @create: 2026-02-26 14:53
 **/
@Repository
public class BizCheckConfigRepositoryImpl extends ServiceImpl<BizCheckConfigMapper, BizCheckConfig> implements BizCheckConfigRepository, BizComponent<BizCheckConfigRepository> {

    @Override
    public String logTag() {
        return "BizCheckConfigRepositoryImpl";
    }

    @Override
    public BizCheckConfig getByBizCode(RunningMonitorTypeEnum bizCode) {
        return this.lambdaQuery().
                eq(BizCheckConfig::getProjectId, ContextUtil.getProjectId()).
                eq(BizCheckConfig::getBizCode, bizCode)
                .last("limit 1")
                .one();
    }

    @Override
    public List<BizCheckConfig> getAll() {
        return this.lambdaQuery().
                eq(BizCheckConfig::getProjectId, ContextUtil.getProjectId())
                .list();
    }
}
