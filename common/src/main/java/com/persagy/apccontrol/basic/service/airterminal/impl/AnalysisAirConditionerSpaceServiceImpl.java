package com.persagy.apccontrol.basic.service.airterminal.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.persagy.apccontrol.basic.config.NacosSwitchConfig;
import com.persagy.apccontrol.basic.mapper.AnalysisAirConditionerSpaceMapper;
import com.persagy.apccontrol.basic.model.entity.AnalysisAirConditionerSpace;
import com.persagy.apccontrol.basic.model.entity.po.AnalysisAirConditionerSpacePO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.service.airterminal.AnalysisAirConditionerSpaceService;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yuyao
 * @description 针对表【analysis_air_conditioner_spaces(运行分析-空调空间分析)】的数据库操作Service实现
 * @createDate 2024-12-03 10:31:19
 */
@Service
public class AnalysisAirConditionerSpaceServiceImpl extends ServiceImpl<AnalysisAirConditionerSpaceMapper, AnalysisAirConditionerSpacePO>
        implements AnalysisAirConditionerSpaceService {

    @Resource
    protected NacosSwitchConfig nacosSwitchConfig;

    @Override
    @Transactional
    public void saveBatchAndDel(List<AnalysisAirConditionerSpace> list) {

        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<AnalysisAirConditionerSpacePO> daoList = AnalysisAirConditionerSpacePO.toAnalysisAirConditionerSpacePOList(list);

        //批量保存
        this.saveBatch(daoList, list.size());

        //取出模版数据
        AnalysisAirConditionerSpacePO entity = daoList.get(0);
        //删除 60 天前的数据 只保留 60 天的数据
        Period period = Period.ofDays(nacosSwitchConfig.getAnalysisDeleteDaysAgoData());

        LocalDate newMonthDay = DateUtils.now().toLocalDate().minus(period);
        this.lambdaUpdate()
                .eq(AnalysisAirConditionerSpacePO::getProjectId, entity.getProjectId())
                .le(AnalysisAirConditionerSpacePO::getDay, newMonthDay)
                .remove();
    }

    @Override
    public boolean deleteByDay(LocalDate day) {
        return this.lambdaUpdate()
                .eq(AnalysisAirConditionerSpacePO::getProjectId, ContextUtil.getProjectId())
                .eq(AnalysisAirConditionerSpacePO::getDay, day)
                .remove();
    }

    @Override
    public List<AnalysisAirConditionerSpace> listByDayAndAbnormalAndTagType(List<String> spaceId, List<String> floorIds,List<String> buildingIds,
                                                                            @NotNull LocalDate day, Boolean abnormal, RunningMonitorSecondTypeEnum tagType) {

        List<AnalysisAirConditionerSpacePO> r = this.lambdaQuery()
                .eq(AnalysisAirConditionerSpacePO::getProjectId, ContextUtil.getProjectId())
                .eq(AnalysisAirConditionerSpacePO::getDay, day)
                .in(CollectionUtils.isNotEmpty(buildingIds), AnalysisAirConditionerSpacePO::getBuildingId, buildingIds)
                .in(CollectionUtils.isNotEmpty(floorIds), AnalysisAirConditionerSpacePO::getFloorId, floorIds)
                .in(CollectionUtils.isNotEmpty(spaceId), AnalysisAirConditionerSpacePO::getSpaceId, spaceId)
                .eq(abnormal != null, AnalysisAirConditionerSpacePO::getAbnormalEvent, abnormal)
                .eq(tagType != null, AnalysisAirConditionerSpacePO::getTagType, tagType)
                .list();

        return AnalysisAirConditionerSpacePO.toAnalysisAirConditionerSpaceList(r);
    }

    @Override
    public Map<String, Set<String>> getSpaceByInfoCodeRule(@NotNull LocalDate day) {
        Map<String, Set<String>> map = new HashMap<>();
        List<AnalysisAirConditionerSpacePO> r = this.lambdaQuery()
                .eq(AnalysisAirConditionerSpacePO::getProjectId, ContextUtil.getProjectId())
                .eq(AnalysisAirConditionerSpacePO::getDay, day)
                .isNotNull(AnalysisAirConditionerSpacePO::getInfoCodeRule)
                .select(AnalysisAirConditionerSpacePO::getSpaceId, AnalysisAirConditionerSpacePO::getInfoCodeRule)
                .list();

        if (CollectionUtils.isEmpty(r)) {
            return map;
        }
        r.forEach(e -> map.put(e.getSpaceId(), e.getInfoCodeRule()));
        return map;

    }

    @Override
    public Double projectSatisfyPercentage(@NotNull LocalDate day, @NotNull RunningMonitorSecondTypeEnum tagType) {
        return getBaseMapper().projectSatisfyPercentage(ContextUtil.getProjectId(), day, tagType);
    }

    @Override
    public Double spaceSatisfyPercentage(LocalDate day, RunningMonitorSecondTypeEnum tagType, Set<String> spaceIds) {
        return getBaseMapper().spaceSatisfyPercentage(ContextUtil.getProjectId(), day, spaceIds, tagType);
    }

    @Override
    public Map<String, Integer> getSpaceTagCount(LocalDate day, RunningMonitorSecondTypeEnum tagType, List<String> spaceIds) {
        List<AnalysisAirConditionerSpacePO> r = this.lambdaQuery()
                .eq(AnalysisAirConditionerSpacePO::getProjectId, ContextUtil.getProjectId())
                .eq(AnalysisAirConditionerSpacePO::getDay, day)
                .in(CollectionUtils.isNotEmpty(spaceIds), AnalysisAirConditionerSpacePO::getSpaceId, spaceIds)
                .eq(tagType != null, AnalysisAirConditionerSpacePO::getTagType, tagType)
                .list();
        return null;
    }
}




