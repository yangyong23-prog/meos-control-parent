package com.persagy.apccontrol.basic.resource.demand.impl;

import com.persagy.apccontrol.basic.config.dao.BaseServiceImpl;
import com.persagy.apccontrol.basic.mapper.demand.DemandTemplateCalendarMapper;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplateCalendar;
import com.persagy.apccontrol.basic.resource.demand.DemandTemplateCalendarService;
import com.persagy.apccontrol.basic.resource.demand.param.DemandTemplateCalendarQueryDaoParam;
import com.persagy.apccontrol.basic.resource.demand.param.DemandTemplateCalendarSaveDaoParam;
import com.persagy.apccontrol.basic.resource.demand.param.DemandTemplateCalendarUpdateDaoParam;
import com.persagy.apccontrol.basic.structure.demand.converter.DemandTemplateCalendarConverter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 需求模版日历服务实现
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Service
@Validated
public class DemandTemplateCalendarServiceImpl
        extends BaseServiceImpl<DemandTemplateCalendarMapper, DemandTemplateCalendar>
        implements DemandTemplateCalendarService {

    @Autowired
    private DemandTemplateCalendarConverter demandTemplateCalendarConverter;

    @Override
    public DemandTemplateCalendar get(
            @NonNull String projectId,
            @NonNull DemandTemplateCalendarQueryDaoParam param) {
        param.setProjectId(projectId);
        return getBaseMapper().get(param);
    }

    @Override
    public List<DemandTemplateCalendar> list(
            @NonNull String projectId,
            @NonNull DemandTemplateCalendarQueryDaoParam param) {
        param.setProjectId(projectId);
        return getBaseMapper().list(param);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(@NonNull String projectId, @NonNull DemandTemplateCalendarSaveDaoParam param) {
        return saveValidate(demandTemplateCalendarConverter.toDo(new DemandTemplateCalendar(), projectId, param));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(@NonNull String projectId, @Nullable Collection<DemandTemplateCalendarSaveDaoParam> params) {
        if (CollectionUtils.isEmpty(params)) {
            return false;
        }
        List<DemandTemplateCalendar> addParams = params.stream()
                .map(item -> demandTemplateCalendarConverter.toDo(new DemandTemplateCalendar(), projectId, item))
                .collect(Collectors.toList());
        return saveValidate(addParams);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean edit(@NonNull String projectId, @NonNull DemandTemplateCalendarUpdateDaoParam param) {
        return updateByIdValidate(demandTemplateCalendarConverter.toDo(
                getByIdValidate(param.getId()), projectId, param));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean edit(@NonNull String projectId, @Nullable Collection<DemandTemplateCalendarUpdateDaoParam> params) {
        if (CollectionUtils.isEmpty(params)) {
            return false;
        }
        Map<Long, DemandTemplateCalendarUpdateDaoParam> idAndParam = params.stream().collect(Collectors.toMap(
                DemandTemplateCalendarUpdateDaoParam::getId, Function.identity(), (o1, o2) -> o1));
        List<DemandTemplateCalendar> dbEntities = listByIdValidate(idAndParam.keySet());
        List<DemandTemplateCalendar> editParams = dbEntities.stream()
                .map(item -> demandTemplateCalendarConverter.toDo(item, projectId, idAndParam.get(item.getId())))
                .collect(Collectors.toList());
        return updateByIdValidate(editParams);
    }
}
