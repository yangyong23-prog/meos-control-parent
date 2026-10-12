package com.persagy.apccontrol.basic.structure.demand.strategy;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import cn.hutool.extra.spring.SpringUtil;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.param.external.demand.ExternalDemandConfigResultQueryParam;
import com.persagy.apccontrol.basic.model.vo.external.demand.ExternalDemandConfigResultVO;
import com.persagy.apccontrol.basic.resource.demand.DemandTemplateService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.dto.DemandInstanceCustomizedTagDTO;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.framework.common.exception.enums.ErrorCodeConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.PriorityOrdered;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 需求管理策略处理器
 *
 * <p>------------------------- 外部方法 -------------------------
 * <ul>
 *     <li><b>getTemplateAndCheckThrow</b> - 查询需求模版并校验</li>
 * </ul>
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
public abstract class AbstractDemandStrategyProcessor implements DemandStrategy, PriorityOrdered, LogComponent {

    private static final String TEMPLATE_NOT_FOUND_MESSAGE = LocaleMessageUtil.getMessage("key_6276688eb1b046f196a4e5ebc536a68d");

    @Override
    public DemandTemplate getTemplateAndCheckThrow(@NonNull String projectId, @NonNull Long templateId) {
        DemandTemplateService demandTemplateService = SpringUtil.getBean(DemandTemplateService.class);
        DemandTemplate dbTemplate = demandTemplateService.getByIdValidate(templateId, TEMPLATE_NOT_FOUND_MESSAGE);
        if (StringUtils.equals(dbTemplate.getProjectId(), projectId)
                && dbTemplate.getSt() == StateTypeEnum.VALID
                && dbTemplate.getDemandType() == demandType()
                && BooleanUtils.isTrue(dbTemplate.getIsEnabled())) {
            return dbTemplate;
        }
        LogExecutor.project(projectId).error(log(), logTag(), TEMPLATE_NOT_FOUND_MESSAGE, Arrays.asList(
                Pair.of("templateId", templateId),
                Pair.of("dbTemplate", dbTemplate)
        ));
        throw new BusinessException(ErrorCodeConstants.USER_ERROR_A0402.getCode(), TEMPLATE_NOT_FOUND_MESSAGE);
    }

    @Override
    public List<DemandTemplateTypeEnum> listTemplateTypes() {
        List<DemandTemplateTypeEnum> templateTypes = demandType().toDemandTemplateTypes();
        if (CollectionUtils.isEmpty(templateTypes)) {
            return Collections.emptyList();
        }
        List<DemandTemplateTypeEnum> results = Collections.synchronizedList(new ArrayList<>());
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (DemandTemplateTypeEnum templateType : templateTypes) {
            futures.add(CompletableFuture.runAsync(() -> {
                DemandSpacesQueryParam spacesQueryParam = new DemandSpacesQueryParam(templateType);
                spacesQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
                List<DtpRwdSpaceDTO> spaces = this.listDemandSpace(spacesQueryParam);
                if (CollectionUtils.isNotEmpty(spaces)) {
                    results.add(templateType);
                }
            }, BusinessThreadPoolExecutor.ttlExecutor()));
        }
        if (CollectionUtils.isNotEmpty(futures)) {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }
        results.sort(Comparator.comparing(templateTypes::indexOf));
        return results;
    }

    @Override
    @Deprecated
    public List<DtpRwdSpaceDTO> listDemandSpaceByInstance(
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull DemandInstanceValueObject instance,
            @Nullable String... withColumns) {
        DemandSpacesQueryParam queryParam = new DemandSpacesQueryParam(templateType);
        queryParam.fillByInstance(instance);
        queryParam.addWithColumn(withColumns);
        return listDemandSpace(queryParam);
    }

    @Override
    public List<DemandInstanceCustomizedTagDTO> mapDemandInstanceCustomizedTagNameAndValue(
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Set<DemandInstanceValueObject> instances) {
        return Lists.newArrayList();
    }

    @Override
    public int getOrder() {
        return demandType().getOrdered();
    }

    @Override
    public List<? extends ExternalDemandConfigResultVO> externalListConfigResultsByInstance(ExternalDemandConfigResultQueryParam queryParam) {
        return Collections.emptyList();
    }
}
