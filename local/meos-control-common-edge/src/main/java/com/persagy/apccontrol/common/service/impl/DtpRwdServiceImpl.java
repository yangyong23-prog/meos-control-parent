package com.persagy.apccontrol.common.service.impl;

import com.persagy.apccontrol.basic.config.ProjectCfg;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFreezePumpDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.param.strategy.StrategyFreezeGroupObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.rwd.DtpRwdRelationResponse;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.RcCalculateResultService;
import com.persagy.apccontrol.basic.service.BizStrategyQueryService;
import com.persagy.apccontrol.common.service.DtpRwdService;
import com.persagy.apccontrol.common.service.command.ManualAutoMaticSetService;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yuyao
 */
@Service
@Slf4j
public class DtpRwdServiceImpl implements DtpRwdService {


    private static final String LOG_TAG = "查询孪生服务";

    @Autowired
    RcCalculateResultService rcCalculateResultService;
    @Autowired
    ManualAutoMaticSetService manualAutoMaticSetService;
    @Autowired
    private ProjectCfg projectCfg;
    @Autowired
    private DtpRwdClientService dtpRwdClientService;

    @Autowired
    private BizStrategyQueryService bizStrategyQueryService;

    @Autowired
    private DtpRwdService dtpRwdService;


    /**
     * 查询末级冷冻泵组关联的空间ID
     *
     * @param acatSystemIdsAndLastFreezePumpGroupId 空调末端系统ID -> 末级冷冻泵组ID
     * @return 末级冷冻泵组ID -> 空调末端系统关联的空间ID
     * @since v5.7.0
     */
    @Override
    public Map<String, Set<String>> queryLastFreezePumpGroupRelationSpaceIds(@NonNull Map<String, Collection<String>> acatSystemIdsAndLastFreezePumpGroupId) {
        Set<String> acatSystemIds = acatSystemIdsAndLastFreezePumpGroupId.keySet();

        List<DtpRwdRelationResponse> relationSpaces = dtpRwdClientService.listRelationObject(false, false, projectCfg.getProjectId(),
                new DtpRwdRelationQueryParam(DtpRwdRelationTypeEnum.SYSTEM_SERVICE_SPACE,
                        new DtpRwdObjectQueryParam().setId(acatSystemIds), null));
        if (CollectionUtils.isEmpty(relationSpaces)) {
            LogExecutor.instance().warn(log, LOG_TAG, "无有效空调末端系统服务的空间", Collections.singletonList(Pair.of("acatSystemIds", acatSystemIds)));
            return null;
        }
        Map<String, Set<String>> result = new HashMap<>(16);
        for (DtpRwdRelationResponse relationSpace : relationSpaces) {
            String acatSystemId = relationSpace.getFromId();
            String spaceId = relationSpace.getToId();
            Collection<String> lastFreezePumpGroupIds = acatSystemIdsAndLastFreezePumpGroupId.get(acatSystemId);
            if (CollectionUtils.isEmpty(lastFreezePumpGroupIds)) {
                continue;
            }
            lastFreezePumpGroupIds.forEach(item -> MapExecutor.dynamicPut(result, item, spaceId, HashSet::new));
        }
        return result;
    }

    /**
     * 查询中央供冷系统ID
     *
     * @return 中央供冷系统ID
     * @since v5.7.0
     */
    @Override
    public Set<String> queryCentralCoolingSystemIds() {
        List<DtpRwdObjectDTO> centralCoolingSystems = dtpRwdClientService.listCentralCoolingSystem(false, false, projectCfg.getProjectId(), null);
        if (CollectionUtils.isEmpty(centralCoolingSystems)) {
            LogExecutor.project(projectCfg.getProjectId()).warn(log, LOG_TAG, "无有效中央供冷系统对象");
            return new HashSet<>();
        }
        return centralCoolingSystems.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
    }

    @Override
    public Set<String> queryHotColdStationSystemIds() {
        List<DtpRwdObjectDTO> centralCoolingSystems = dtpRwdClientService.listCentralCoolingHeatingSystem(
                false, false, projectCfg.getProjectId(), null);
        if (CollectionUtils.isEmpty(centralCoolingSystems)) {
            LogExecutor.instance().warn(log, LOG_TAG, "无有效系统对象");
            return new HashSet<>();
        }
        return centralCoolingSystems.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
    }

    @Override
    public Set<String> getFreezePump(Set<String> systemId) {
        Set<String> s = new HashSet<>();

        if (CollectionUtils.isEmpty(systemId)) {
            return s;
        }

        StrategyFreezeGroupObjectQueryParam dd = new StrategyFreezeGroupObjectQueryParam();
        dd.setProjectId(projectCfg.getProjectId());
        List<DtpRwdFreezePumpDTO> dtpRwdFreezePumpDTO = bizStrategyQueryService.listLastFreezePumpGroups(dd);

        if (CollectionUtils.isEmpty(dtpRwdFreezePumpDTO)) {
            return s;
        }

        systemId.forEach(i -> dtpRwdFreezePumpDTO.forEach(b -> {
            if (StringUtils.equals(i, b.getRelationAcccId())) {
                s.add(b.getId());
            }
        }));

        return s;
    }

    @Override
    public Map<String, String> getSystemIdByFreezePumpId() {
        //泵组和系统的关系 key 是泵组id val 是系统id
        Map<String, String> gx = new HashMap<>();

        StrategyFreezeGroupObjectQueryParam dd = new StrategyFreezeGroupObjectQueryParam();
        dd.setProjectId(projectCfg.getProjectId());
        List<DtpRwdFreezePumpDTO> dtpRwdFreezePumpDTO = bizStrategyQueryService.listLastFreezePumpGroups(dd);

        if (CollectionUtils.isEmpty(dtpRwdFreezePumpDTO)) {
            LogExecutor.project(projectCfg.getProjectId()).warn(log, LOG_TAG, "未查询到末级泵组");
            return gx;
        }

        Map<String, List<DtpRwdFreezePumpDTO>> collect = dtpRwdFreezePumpDTO.stream().collect(Collectors.groupingBy(DtpRwdFreezePumpDTO::getRelationAcccId));

        // 查询中央供冷系统ID
        Set<String> centralCoolingSystemIds = dtpRwdService.queryCentralCoolingSystemIds();
        if (CollectionUtils.isEmpty(centralCoolingSystemIds)) {
            return gx;
        }
        for (String centralCoolingSystemId : centralCoolingSystemIds) {
            // 查询末级冷冻泵
            List<DtpRwdFreezePumpDTO> lastFreezePumpGroups = collect.get(centralCoolingSystemId);
            if (CollectionUtils.isEmpty(lastFreezePumpGroups)) {
                continue;
            }
            for (DtpRwdFreezePumpDTO lastFreezePumpGroup : lastFreezePumpGroups) {
                gx.put(lastFreezePumpGroup.getId(), centralCoolingSystemId);
            }
        }
        return gx;
    }

    @Override
    public String getSystemIdByFreezePumpId(String objId) {
        String syId = objId;

        DtpRwdObjectDTO objectDTO = dtpRwdClientService.getObject(true, true,
                projectCfg.getProjectId(), new DtpRwdObjectQueryParam().setId(objId));

        //泵组单独处理
        if (StringUtils.equals(objectDTO.getClassCode(), DtpRwdClassCodeEnum.ACCPC_GROUP.getValue())) {
            Map<String, String> gx = dtpRwdService.getSystemIdByFreezePumpId();
            syId = gx.get(objId);
        }
        return syId;
    }

}
