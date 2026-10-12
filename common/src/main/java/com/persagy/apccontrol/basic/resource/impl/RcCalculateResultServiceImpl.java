package com.persagy.apccontrol.basic.resource.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.persagy.apccontrol.basic.mapper.RcCalculateResultMapper;
import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.entity.RcCalculateResult;
import com.persagy.apccontrol.basic.resource.RcCalculateResultService;
import com.persagy.framework.database.service.SuperServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 价值计算结果
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/9/7 18:43
 */
@Service
public class RcCalculateResultServiceImpl extends SuperServiceImpl<RcCalculateResultMapper, RcCalculateResult> implements RcCalculateResultService {

    @Override
    public List<RcCalculateResult> queryCalculateResultList(String groupCode, String projectId, Set<String> spaceId, String workDate, BasicsConfigTypeEnum calculateType) {
        List<RcCalculateResult> calculateResults = this.lambdaQuery().eq(StrUtil.isNotBlank(groupCode), RcCalculateResult::getGroupCode, groupCode)
                .eq(StrUtil.isNotBlank(projectId), RcCalculateResult::getProjectId, projectId).in(CollectionUtil.isNotEmpty(spaceId), RcCalculateResult::getSpaceId, spaceId)
                .eq(StrUtil.isNotBlank(workDate), RcCalculateResult::getWorkDate, workDate).eq(calculateType != null, RcCalculateResult::getCalculateType, calculateType)
                .list();
        return calculateResults;
    }

    @Override
    public Set<String> getSpaceCalculateResultIds(String groupCode, String projectId, String workDate) {
        List<RcCalculateResult> calculateResults = this.lambdaQuery().eq(StrUtil.isNotBlank(groupCode), RcCalculateResult::getGroupCode, groupCode)
                .eq(StrUtil.isNotBlank(projectId), RcCalculateResult::getProjectId, projectId).eq(StrUtil.isNotBlank(workDate), RcCalculateResult::getWorkDate, workDate)
                .select(RcCalculateResult::getSpaceId).list();
        return CollectionUtils.isEmpty(calculateResults) ? new HashSet<>() : calculateResults.stream().map(RcCalculateResult::getSpaceId).collect(Collectors.toSet());
    }

    @Override
    public RcCalculateResult getSpaceCalculateResult(String groupCode, String projectId, String spaceId, String workDate) {
        return this.lambdaQuery().eq(StrUtil.isNotBlank(groupCode), RcCalculateResult::getGroupCode, groupCode)
                .eq(StrUtil.isNotBlank(projectId), RcCalculateResult::getProjectId, projectId).eq(StrUtil.isNotBlank(workDate), RcCalculateResult::getWorkDate, workDate)
                .eq(StrUtil.isNotBlank(spaceId), RcCalculateResult::getSpaceId, spaceId).last(CommonConst.LIMIT_ONE).one();
    }

    @Override
    public List<RcCalculateResult> queryMultiDayCalculateResultList(String groupCode,
                                                                    String projectId,
                                                                    List<String> spaceId,
                                                                    List<String> workDate,
                                                                    BasicsConfigTypeEnum calculateType) {
        return this.lambdaQuery()
                .eq(StrUtil.isNotBlank(groupCode), RcCalculateResult::getGroupCode, groupCode)
                .eq(StrUtil.isNotBlank(projectId), RcCalculateResult::getProjectId, projectId)
                .in(CollectionUtil.isNotEmpty(spaceId), RcCalculateResult::getSpaceId, spaceId)
                .in(CollectionUtils.isNotEmpty(workDate), RcCalculateResult::getWorkDate, workDate)
                .eq(calculateType != null, RcCalculateResult::getCalculateType, calculateType)
                .select(RcCalculateResult::getCalculateResult, RcCalculateResult::getSpaceId, RcCalculateResult::getWorkDate)
                .orderByDesc(RcCalculateResult::getWorkDate).list();
    }

}




