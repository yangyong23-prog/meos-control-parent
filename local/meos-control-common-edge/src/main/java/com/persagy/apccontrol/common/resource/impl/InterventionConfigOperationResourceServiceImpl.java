package com.persagy.apccontrol.common.resource.impl;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.config.dao.BaseServiceImpl;
import com.persagy.apccontrol.basic.convert.InterventionConvert;
import com.persagy.apccontrol.basic.mapper.intervention.InterventionConfigMapper;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.entity.intervention.InterventionAddEntity;
import com.persagy.apccontrol.basic.model.entity.intervention.InterventionConfig;
import com.persagy.apccontrol.basic.model.entity.intervention.TimeSlotEntity;
import com.persagy.apccontrol.basic.model.enumtype.system.DataTypeEnum;
import com.persagy.apccontrol.basic.resource.intervention.impl.InterventionConfigQueryResourceServiceImpl;
import com.persagy.apccontrol.basic.resource.intervention.param.InterventionConfigQueryDaoParam;
import com.persagy.apccontrol.basic.model.param.datatransfer.PaasDataTransferParam;
import com.persagy.apccontrol.basic.outside.feign.service.PaasDataTransferClientService;
import com.persagy.apccontrol.common.resource.InterventionConfigOperationResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 干预配置操作服务实现
 *
 * @author JiYinchuan
 * @since 5.241102.0
 */
@Service
@Slf4j
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InterventionConfigOperationResourceServiceImpl
        extends BaseServiceImpl<InterventionConfigMapper, InterventionConfig>
        implements InterventionConfigOperationResourceService {

    private final InterventionConvert interventionConvert;
    private final PaasDataTransferClientService paasDataTransferClientService;
    private final InterventionConfigQueryResourceServiceImpl interventionConfigQueryResourceService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean editEndTime(@NonNull Long id, @NonNull LocalDateTime endTime) {
        InterventionConfig editConfig = new InterventionConfig();
        editConfig.setId(id);
        editConfig.setEndTime(endTime);
        boolean result = updateByIdValidate(editConfig);
        interventionConfigQueryResourceService.get(new InterventionConfigQueryDaoParam().setId(id))
                .ifPresent(interventionConfig -> paasDataTransferClientService.syncDbDataToCloud(
                        PaasDataTransferParam.of(DataTypeEnum.EDIT, interventionConfig)));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean del(@NonNull Long id) {
        boolean result = removeByIdIgnore(id);
        paasDataTransferClientService.syncDbDataToCloud(PaasDataTransferParam.ofDel(
                Pair.of(new InterventionConfig(), id)));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> addInterventionConfig(InterventionAddEntity entity) {
        List<TimeSlotEntity> timeSlot = entity.getTimeSlot();
        if (CollectionUtils.isEmpty(timeSlot)) {
           return Lists.newArrayList();
        }
        List<InterventionConfig> configs = timeSlot.stream().map(time -> {
            InterventionConfig config = interventionConvert.addEntityToPo(entity);
            config.setBeginTime(time.getBeginDateTime());
            config.setEndTime(time.getEndDateTime());
            return config;
        }).collect(Collectors.toList());
        saveOrUpdateBatch(configs);
        List<Long> ids = configs.stream().map(BaseAssignIdDO::getId).collect(Collectors.toList());
        configs = interventionConfigQueryResourceService.list(new InterventionConfigQueryDaoParam().setIds(ids));
        paasDataTransferClientService.syncDbDataToCloud(PaasDataTransferParam.of(DataTypeEnum.ADD, configs));
        return ids;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteInterventionConfigByInstanceIds(Collection<String> instanceIds) {
        Set<Long> removeIds = lambdaQuery()
                .select(BaseAssignIdDO::getId)
                .in(InterventionConfig::getInstanceId, instanceIds)
                .list().stream()
                .map(BaseAssignIdDO::getId)
                .collect(Collectors.toSet());
        removeByIdIgnore(removeIds);
        paasDataTransferClientService.syncDbDataToCloud(PaasDataTransferParam.ofDel(removeIds.stream()
                .map(item -> Pair.of(new InterventionConfig(), item)).collect(Collectors.toList())));
    }
}
