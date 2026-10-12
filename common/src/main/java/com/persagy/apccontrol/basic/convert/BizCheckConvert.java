package com.persagy.apccontrol.basic.convert;

import com.fasterxml.jackson.databind.JsonNode;
import com.persagy.apccontrol.basic.model.dto.StartEndDayDTO;
import com.persagy.apccontrol.basic.model.param.bizcheck.BizCheckConfigParam;
import com.persagy.apccontrol.basic.model.param.bizcheck.NotifyPeriodConfigParam;
import com.persagy.apccontrol.basic.model.vo.bizcheck.BizCheckConfigVO;
import com.persagy.apccontrol.basic.model.vo.bizcheck.BizCheckResultVO;
import com.persagy.apccontrol.basic.structure.bizcheck.model.dto.BizCheckItemDTO;
import com.persagy.apccontrol.basic.structure.bizcheck.model.entity.BizCheckConfig;
import com.persagy.apccontrol.basic.structure.bizcheck.model.entity.BizCheckResult;
import com.persagy.apccontrol.basic.utils.ObjectMapperUtils;
import org.mapstruct.*;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @description:
 * @author: wxf
 * @create: 2026-02-26 15:02
 **/
@Mapper(componentModel = Constant.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface BizCheckConvert {

        @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "createUser", ignore = true)
    @Mapping(target = "updateUser", ignore = true)
    @Mapping(target = "projectId", ignore = true)
    @Mapping(target = "dateRangeConfig", source = "dateRangeConfig", qualifiedByName = "dateRangeConfigToString")
    @Mapping(target = "notifyPeriodConfig", source = "notifyPeriodConfig", qualifiedByName = "notifyPeriodConfigToString")
    BizCheckConfig toConfigEntity(BizCheckConfigParam param);

    @Mapping(target = "dateRangeConfig", source = "dateRangeConfig", qualifiedByName = "stringToDateRangeConfig")
    @Mapping(target = "notifyPeriodConfig", source = "notifyPeriodConfig", qualifiedByName = "stringToNotifyPeriodConfig")
    BizCheckConfigVO toConfigVO(BizCheckConfig entity);

    @AfterMapping
    default void setConditionalId(@MappingTarget BizCheckConfig entity, BizCheckConfigParam param) {
        if (param.getId() == null) {
            entity.setId(null);
        } else {
            entity.setId(param.getId());
        }
    }

    @Named("dateRangeConfigToString")
    default String dateRangeConfigToString(List<StartEndDayDTO> dateRangeConfig) {
        if (dateRangeConfig == null || dateRangeConfig.isEmpty()) {
            return "[]";
        }
        return ObjectMapperUtils.writeValueAsStringFailSafe(dateRangeConfig);
    }

    @Named("notifyPeriodConfigToString")
    default String notifyPeriodConfigToString(List<NotifyPeriodConfigParam> notifyPeriodConfig) {
        if (notifyPeriodConfig == null || notifyPeriodConfig.isEmpty()) {
            return "[]";
        }
        return ObjectMapperUtils.writeValueAsStringFailSafe(notifyPeriodConfig);
    }

    @Named("stringToDateRangeConfig")
    default List<StartEndDayDTO> stringToDateRangeConfig(String dateRangeConfig) {
        if (dateRangeConfig == null || dateRangeConfig.isEmpty() || "[]".equals(dateRangeConfig) || "null".equals(dateRangeConfig)) {
            return new ArrayList<>();
        }
        try {
            return ObjectMapperUtils.readValue(dateRangeConfig, new com.fasterxml.jackson.core.type.TypeReference<List<StartEndDayDTO>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Named("stringToNotifyPeriodConfig")
    default List<NotifyPeriodConfigParam> stringToNotifyPeriodConfig(String notifyPeriodConfig) {
        if (notifyPeriodConfig == null || notifyPeriodConfig.isEmpty() || "[]".equals(notifyPeriodConfig) || "null".equals(notifyPeriodConfig)) {
            return new ArrayList<>();
        }
        try {
            return ObjectMapperUtils.readValue(notifyPeriodConfig, new com.fasterxml.jackson.core.type.TypeReference<List<NotifyPeriodConfigParam>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * 检查结果实体转VO
     */
    @Mapping(target = "checkResult", source = "checkResult", qualifiedByName = "stringToCheckResult")
    BizCheckResultVO toResultVO(BizCheckResult entity);

    @Named("stringToCheckResult")
    default JsonNode stringToCheckResult(String checkResult) {
        if (checkResult == null || checkResult.isEmpty() || "[]".equals(checkResult) || "null".equals(checkResult)) {
            return ObjectMapperUtils.getObjectMapper().createArrayNode();
        }
        try {
            return ObjectMapperUtils.readValue(checkResult, JsonNode.class);
        } catch (Exception e) {
            return ObjectMapperUtils.getObjectMapper().createArrayNode();
        }
    }
}