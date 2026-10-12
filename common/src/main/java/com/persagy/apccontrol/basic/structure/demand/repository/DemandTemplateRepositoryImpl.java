package com.persagy.apccontrol.basic.structure.demand.repository;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.resource.demand.DemandTemplateService;
import com.persagy.apccontrol.basic.resource.demand.param.DemandTemplateQueryDaoParam;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.enumeration.error.client.RequestParamErrorEnum;
import com.persagy.validate.utility.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 需求模版仓储
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Repository
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandTemplateRepositoryImpl implements DemandTemplateRepository {

    private final DemandTemplateService demandTemplateService;

    @Override
    public DemandTemplate getByTemplateId(boolean isThrows, @NonNull Long templateId) {
        Optional<DemandTemplate> templateOpt = demandTemplateService.getByIdIgnoreOpt(templateId);
        if (isThrows && !templateOpt.isPresent()) {
            throw new ApplicationException(RequestParamErrorEnum.INVALID_INPUT, LocaleMessageUtil.getMessage("key_6276688eb1b046f196a4e5ebc536a68d"));
        }
        return templateOpt.orElse(null);
    }

    @Override
    public DemandTemplate getByTemplateIdAndDate(
            @NonNull boolean isThrows,
            @NonNull Long templateId,
            @Nullable LocalDate date) {
        if (Objects.isNull(date) || !date.isBefore(DateUtils.now().toLocalDate())) {
            return getByTemplateId(isThrows, templateId);
        }
        DemandTemplateQueryDaoParam queryDaoParam = new DemandTemplateQueryDaoParam();
        queryDaoParam.setId(templateId);
        queryDaoParam.setDate(date);
        Optional<DemandTemplate> templateOpt = demandTemplateService.get(queryDaoParam);
        if (isThrows && !templateOpt.isPresent()) {
            throw new ApplicationException(RequestParamErrorEnum.INVALID_INPUT, LocaleMessageUtil.getMessage("key_6276688eb1b046f196a4e5ebc536a68d"));
        }
        return templateOpt.orElse(null);
    }

    @Override
    public DemandTemplate getByTemplateByTemplateTypeTag(
            boolean isThrows,
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull String templateTag) {
        Optional<DemandTemplate> templateOpt = demandTemplateService.lambdaQuery()
                .eq(DemandTemplate::getProjectId, ContextUtil.getProjectId())
                .eq(DemandTemplate::getDemandType, templateType.toDemandTypeEnum())
                .eq(DemandTemplate::getDemandTemplateType, templateType)
                .eq(DemandTemplate::getDemandTemplateTag, templateTag)
                .oneOpt();
        if (isThrows && !templateOpt.isPresent()) {
            throw new ApplicationException(RequestParamErrorEnum.INVALID_INPUT, LocaleMessageUtil.getMessage("key_6276688eb1b046f196a4e5ebc536a68d"));
        }
        return templateOpt.orElse(null);
    }

    @Override
    public List<DemandTemplate> listByTemplateIds(@NonNull Set<Long> templateIds) {
        return demandTemplateService.listByIdIgnore(templateIds);
    }

    @Override
    public List<Long> listTemplateIdsByTemplateType(@NonNull DemandTemplateTypeEnum templateType) {
        DemandTemplateQueryDaoParam queryDaoParam = new DemandTemplateQueryDaoParam();
        queryDaoParam.setDemandTemplateType(templateType);
        queryDaoParam.setDemandType(templateType.toDemandTypeEnum());
        queryDaoParam.addColumns(BaseAssignIdDO::getId);
        return demandTemplateService.list(queryDaoParam).stream().map(BaseAssignIdDO::getId).collect(Collectors.toList());
    }

    @Override
    public String logTag() {
        return "需求管理-需求模版仓储";
    }
}
