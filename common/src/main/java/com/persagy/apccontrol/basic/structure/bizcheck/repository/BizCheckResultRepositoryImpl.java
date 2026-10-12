package com.persagy.apccontrol.basic.structure.bizcheck.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.service.BizComponent;
import com.persagy.apccontrol.basic.structure.bizcheck.model.entity.BizCheckResult;
import com.persagy.apccontrol.basic.structure.bizcheck.repository.mapper.BizCheckResultMapper;
import com.persagy.framework.common.context.ContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 业务检查结果仓储实现类
 *
 * @author wxf
 * @date 2026-02-27
 */
@Repository
@Slf4j
public class BizCheckResultRepositoryImpl extends ServiceImpl<BizCheckResultMapper, BizCheckResult> 
        implements BizCheckResultRepository, BizComponent<BizCheckResultRepository> {

    @Override
    public String logTag() {
        return "BizCheckResultRepositoryImpl";
    }

    @Override
    public List<BizCheckResult> listByBizCode(RunningMonitorTypeEnum bizCode) {
        return this.lambdaQuery()
                .eq(BizCheckResult::getProjectId, ContextUtil.getProjectId())
                .eq(BizCheckResult::getBizCode, bizCode.getValue())
                .list();
    }

    @Override
    public boolean deleteByBizCode(RunningMonitorTypeEnum bizCode) {
        return this.lambdaUpdate()
                .eq(BizCheckResult::getProjectId, ContextUtil.getProjectId())
                .eq(BizCheckResult::getBizCode, bizCode.getValue())
                .remove();
    }
}