package com.persagy.apccontrol.basic.outside.feign.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.base.CacheKey;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDataSourceDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtpknowledge.DtpKnowledgeTypeCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.terminal.ObjectTypeEnum;
import com.persagy.apccontrol.basic.outside.BaseFeignClientService;
import com.persagy.apccontrol.basic.outside.feign.client.DtpKnowledgeServerClient;
import com.persagy.apccontrol.basic.outside.feign.model.param.knowledge.DtpKnowledgeClassCodeByTagQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.knowledge.DtpKnowledgeClassCodeTagQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.knowledge.DtpKnowledgeInfoCodeByTagQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.knowledge.*;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.structure.knowledge.converter.DtpKnowledgeConverter;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.entity.R;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.framework.common.exception.enums.ErrorCodeConstants;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 类功能描述
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/11/10 19:07
 */
@Service
@Slf4j
public class DtpKnowledgeClientServiceImpl implements DtpKnowledgeClientService, BaseFeignClientService {

    public static final String LOG_TAG = "物理世界字典服务";

    @Autowired
    @Lazy
    private DtpKnowledgeClientService dtpKnowledgeClientService;
    @Autowired
    private DtpKnowledgeServerClient dtpKnowledgeServerClient;
    @Autowired
    private DtpKnowledgeConverter dtpKnowledgeConverter;

    @Override
    public DtpKnowledgeObjectClassCodeResponse getClassCode(
            boolean queryCache, boolean isThrows, @NonNull String classCodes) {
        if (StringUtils.isBlank(classCodes)) {
            return null;
        }
        return getOne(isThrows, dtpKnowledgeClientService.listClassCodes(
                queryCache, isThrows, Lists.newArrayList(classCodes)));
    }

    @Override
    public String getClassCodeName(boolean queryCache, boolean isThrows, @NonNull String classCode) {
        if (StringUtils.isBlank(classCode)) {
            return null;
        }
        DtpKnowledgeObjectClassCodeResponse classCodeDefine =
                dtpKnowledgeClientService.getClassCode(queryCache, isThrows, classCode);
        if (Objects.isNull(classCodeDefine)) {
            return null;
        }
        return classCodeDefine.getName();
    }

    @Override
    public @Nullable DtpKnowledgeInfoCodeDTO getInfoCode(@NonNull String classCode, @NonNull String infoCode) {
        return getInfoCode(false, classCode, infoCode);
    }

    @Override
    @Nullable
    public DtpKnowledgeInfoCodeDTO getInfoCode(
            boolean isThrows,
            @NonNull String classCode,
            @NonNull String infoCode) {
        DtpKnowledgeObjectInfoCodeResponse response = getOne(isThrows, query(isThrows,
                null, null, LocaleMessageUtil.getMessage("key_0bbf7003833946f48b0431fe9aef1832"), String.join("-", classCode, infoCode),
                () -> dtpKnowledgeServerClient.listInfoCode(classCode, infoCode), ArrayList::new));
        if (Objects.isNull(response)) {
            return null;
        }
        return dtpKnowledgeConverter.toInfoCodeDto(response);
    }

    @Override
    public List<DtpKnowledgeTypeResponse> listTypeDefines(@NonNull DtpKnowledgeTypeCodeEnum typeCode) {
        return this.query(false, "Type Defines", typeCode,
                () -> dtpKnowledgeServerClient.listTypeDefines(typeCode.value()), ArrayList::new);
    }

    @Override
    public List<DtpKnowledgeObjectClassCodeResponse> listClassCodes(@NonNull Collection<String> classCodes) {
        return listClassCodes(false, false, classCodes);
    }

    @Override
    @Cacheable(cacheNames = CacheKey.Dynamic.KNOWLEDGE_CLASS_CODE, key = "#classCodes?.hashCode()", condition = "#queryCache"
            , unless = "#result == null || #result.isEmpty()")
    public List<DtpKnowledgeObjectClassCodeResponse> listClassCodes(
            boolean queryCache, boolean isThrows, @NonNull Collection<String> classCodes) {
        if (CollectionUtils.isEmpty(classCodes)) {
            return Lists.newArrayList();
        }
        return query(isThrows, null, null, LocaleMessageUtil.getMessage("key_d6acffe602ff48059ec4183988b1d0f7"), classCodes,
                () -> dtpKnowledgeServerClient.listClassCode(classCodes), ArrayList::new);
    }

    @Override
    public List<DtpKnowledgeClassCodeByTagResponse> listClassCodesByLastLevelTag(
            @NonNull ObjectTypeEnum objectType,
            @NonNull Map<String, Set<String>> typeCodeAndTagCode) {
        if (MapUtils.isEmpty(typeCodeAndTagCode)) {
            return Lists.newArrayList();
        }
        List<DtpKnowledgeClassCodeByTagQueryParam.TagParam> tagParams = typeCodeAndTagCode.entrySet().stream()
                .map(item -> {
                    DtpKnowledgeClassCodeByTagQueryParam.TagParam tagParam =
                            new DtpKnowledgeClassCodeByTagQueryParam.TagParam();
                    tagParam.setParamType(0);
                    tagParam.setTypeCode(item.getKey());
                    tagParam.setTagCode(item.getValue());
                    return tagParam;
                }).collect(Collectors.toList());
        DtpKnowledgeClassCodeByTagQueryParam queryParam = new DtpKnowledgeClassCodeByTagQueryParam();
        queryParam.setObjType(objectType.value());
        queryParam.setTagParams(tagParams);
        return query(false, null, null, LocaleMessageUtil.getMessage("key_2e8a4d619c704adf9f70d2f9371f426e"), queryParam,
                () -> dtpKnowledgeServerClient.listClassCodesByTag(queryParam), ArrayList::new);
    }

    @Override
    public List<DtpKnowledgeClassCodeTagResponse> listClassCodeTags(@NonNull DtpKnowledgeClassCodeTagQueryParam param) {
        return query(false, null, null, "Query ClassCode Tags", param,
                () -> dtpKnowledgeServerClient.listClassCodeTags(param), ArrayList::new);
    }

    @Override
    public Map<String, List<DtpKnowledgeInfoCodeTagResponse>> listInfoByTag(DtpKnowledgeInfoCodeByTagQueryParam param) {
        return query(false, null, null, "Query InfoCode Tags", param,
                () -> dtpKnowledgeServerClient.listInfoByTag(param),  HashMap::new);

    }

    @Override
    public List<DtpKnowledgeInfoCodeDTO> listInfoCodes(
            boolean queryCache, boolean isThrows,
            @NonNull Set<String> classCodes) {
        Map<String, List<DtpKnowledgeInfoCodeDTO>> classCodeAndInfoCodes =
                dtpKnowledgeClientService.mapClassCodeAndInfoCodeDefines(
                        queryCache, isThrows, classCodes);
        for (String classCode : classCodes) {
            if (!classCodeAndInfoCodes.containsKey(classCode)) {
                LogExecutor.rpc(getGroupCode(), classCode, classCodeAndInfoCodes).warn(log(), logTag(), "未查询到 [类型编码]");
                if (isThrows) {
                    throw new BusinessException(ErrorCodeConstants.USER_ERROR_A0402.getCode(), LocaleMessageUtil.getMessage("key_6f6b84c2dbca4da2885d771c2e46a873"));
                }
            }
        }
        return classCodeAndInfoCodes.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    public List<DtpKnowledgeInfoCodeDTO> listInfoCodes(
            boolean queryCache, boolean isThrows,
            @NonNull String classCode) {
        Map<String, List<DtpKnowledgeInfoCodeDTO>> classCodeAndInfoCodes =
                dtpKnowledgeClientService.mapClassCodeAndInfoCodeDefines(
                        queryCache, isThrows, Lists.newArrayList(classCode));
        if (!classCodeAndInfoCodes.containsKey(classCode)) {
            LogExecutor.rpc(getGroupCode(), classCode, classCodeAndInfoCodes).warn(log(), logTag(), "未查询到 [类型编码]");
            if (isThrows) {
                throw new BusinessException(ErrorCodeConstants.USER_ERROR_A0402.getCode(), LocaleMessageUtil.getMessage("key_6f6b84c2dbca4da2885d771c2e46a873"));
            }
        }
        return Optional.ofNullable(MapUtils.getObject(classCodeAndInfoCodes, classCode)).orElseGet(ArrayList::new);
    }

    @Override
    public Map<String, DtpKnowledgeInfoCodeDTO> listInfoCodesMap(
            boolean queryCache, boolean isThrows,
            @NonNull String classCode) {

        List<DtpKnowledgeInfoCodeDTO> dtpKnowledgeInfoCodeDTOS = listInfoCodes(queryCache, isThrows, classCode);
        Map<String, DtpKnowledgeInfoCodeDTO> dtpKnowledgeInfoCodeDTOMap = new HashMap<>();
        for (DtpKnowledgeInfoCodeDTO dtpKnowledgeInfoCodeDTO : dtpKnowledgeInfoCodeDTOS) {
            dtpKnowledgeInfoCodeDTOMap.put(dtpKnowledgeInfoCodeDTO.getCode(), dtpKnowledgeInfoCodeDTO);
        }
        return dtpKnowledgeInfoCodeDTOMap;
    }

    @Override
    public Map<String, String> mapClassCodeAndName(
            boolean queryCache, boolean isThrows,
            @NonNull Collection<String> classCodes) {
        if (CollectionUtils.isEmpty(classCodes)) {
            return Maps.newHashMap();
        }
        return dtpKnowledgeClientService.listClassCodes(queryCache, isThrows, classCodes).stream()
                .collect(Collectors.toMap(DtpKnowledgeObjectClassCodeResponse::getCode,
                        DtpKnowledgeObjectClassCodeResponse::getName, (o1, o2) -> o1));
    }

    @Override
    public Map<String, DtpKnowledgeObjectClassCodeResponse> mapClassCodeAndDefine(
            boolean isThrows, @NonNull Collection<String> classCodes) {
        if (CollectionUtils.isEmpty(classCodes)) {
            return Maps.newHashMap();
        }
        return dtpKnowledgeClientService.listClassCodes(false, isThrows, classCodes).stream()
                .collect(Collectors.toMap(DtpKnowledgeObjectClassCodeResponse::getCode,
                        Function.identity(), (o1, o2) -> o1));
    }

    @Override
    public Map<String, String> mapInfoCodeAndName(boolean queryCache, boolean isThrows, @NonNull String classCode) {
        if (StringUtils.isBlank(classCode)) {
            return Maps.newHashMap();
        }
        return this.dtpKnowledgeClientService.listInfoCodes(queryCache, isThrows, classCode).stream()
                .collect(Collectors.toMap(DtpKnowledgeInfoCodeDTO::getCode,
                        DtpKnowledgeInfoCodeDTO::getName, (o1, o2) -> o1));
    }

    @Override
    @NonNull
    public Map<String, String> mapInfoCodeAndName(@NonNull String classCode) {
        if (StringUtils.isBlank(classCode)) {
            return Maps.newHashMap();
        }
        List<DtpKnowledgeInfoCodeDTO> infoCodes = this.dtpKnowledgeClientService
                .mapClassCodeAndInfoCodeDefines(Collections.singleton(classCode))
                .getOrDefault(classCode, Collections.emptyList());
        return infoCodes.stream().collect(Collectors.toMap(DtpKnowledgeInfoCodeDTO::getCode,
                DtpKnowledgeInfoCodeDTO::getName, (o1, o2) -> o1));
    }

    @Override
    public Map<String, DtpKnowledgeInfoCodeDTO> mapInfoCodeAndData(
            boolean queryCache, boolean isThrows, @NonNull String classCode) {
        if (StringUtils.isBlank(classCode)) {
            return Maps.newHashMap();
        }
        return dtpKnowledgeClientService.listInfoCodes(queryCache, isThrows, classCode).stream()
                .collect(Collectors.toMap(DtpKnowledgeInfoCodeDTO::getCode, Function.identity(), (o1, o2) -> o1));
    }

    @Override
    public Map<String, List<DtpKnowledgeInfoCodeDataSourceDTO>> mapInfoCodeAndDataSources(
            boolean queryCache, boolean isThrows, @NonNull String classCode) {
        List<DtpKnowledgeInfoCodeDTO> infoCodes =
                dtpKnowledgeClientService.listInfoCodes(queryCache, isThrows, classCode);
        if (CollectionUtils.isEmpty(infoCodes)) {
            return Maps.newHashMap();
        }
        Map<String, List<DtpKnowledgeInfoCodeDataSourceDTO>> result = new HashMap<>(infoCodes.size());
        for (DtpKnowledgeInfoCodeDTO infoCode : infoCodes) {
            String code = infoCode.getCode();
            List<DtpKnowledgeInfoCodeDataSourceDTO> dataSources = infoCode.getDataSources();
            if (Objects.nonNull(dataSources)) {
                result.put(code, dataSources);
            }
        }
        return result;
    }

    @Override
    public Map<String, List<DtpKnowledgeInfoCodeDTO>> mapClassCodeAndInfoCodeDefines(
            @NonNull Collection<String> classCodes) {
        return this.mapClassCodeAndInfoCodeDefines(false, false, classCodes);
    }

    @Override
    @Cacheable(cacheNames = CacheKey.Dynamic.KNOWLEDGE_INFO_CODE_LIST, key = "#classCodes?.hashCode()", condition = "#queryCache"
            , unless = "#result == null || #result.isEmpty()")
    public Map<String, List<DtpKnowledgeInfoCodeDTO>> mapClassCodeAndInfoCodeDefines(
            boolean queryCache, boolean isThrows,
            @NonNull Collection<String> classCodes) {
        if (CollectionUtils.isEmpty(classCodes)) {
            return Maps.newHashMap();
        }
        // 接口限制最大每次查询20
        int MAX_SIZE_LIMIT = 20;
        List<List<String>> classCodePartitions = Lists.partition(new ArrayList<>(classCodes), MAX_SIZE_LIMIT);
        Map<String, List<DtpKnowledgeInfoCodeDTO>> result = new LinkedHashMap<>();
        for (List<String> classCodePartition : classCodePartitions) {
            Map<String, List<DtpKnowledgeObjectInfoCodeResponse>> classCodeAndInfoCodes = getOne(isThrows, query(isThrows,
                    null, null, LocaleMessageUtil.getMessage("key_0bbf7003833946f48b0431fe9aef1832"), classCodePartition,
                    () -> dtpKnowledgeServerClient.listInfoCode(classCodePartition), ArrayList::new));
            if (MapUtils.isNotEmpty(classCodeAndInfoCodes)) {
                classCodeAndInfoCodes.forEach((classCode, infoCodeDefines) ->
                        result.put(classCode, dtpKnowledgeConverter.toInfoCodeDto(infoCodeDefines)));
            }
        }
        return result;
    }

    @Override
    public Map<String, Map<String, DtpKnowledgeInfoCodeDTO>> mapClassCodeInfoCodeAndDefine(
            @NonNull Collection<String> classCodes) {
        return mapClassCodeInfoCodeAndDefine(false, false, classCodes);
    }

    @Override
    @Cacheable(cacheNames = CacheKey.Dynamic.KNOWLEDGE_INFO_CODE_MAP, key = "#classCodes?.hashCode()", condition = "#queryCache"
            , unless = "#result == null || #result.isEmpty()")
    public Map<String, Map<String, DtpKnowledgeInfoCodeDTO>> mapClassCodeInfoCodeAndDefine(
            boolean queryCache, boolean isThrows,
            @NonNull Collection<String> classCodes) {
        Map<String, List<DtpKnowledgeInfoCodeDTO>> classCodeAndInfoCodes =
                mapClassCodeAndInfoCodeDefines(queryCache, isThrows, classCodes);
        if (MapUtils.isEmpty(classCodeAndInfoCodes)) {
            return Maps.newHashMap();
        }
        Map<String, Map<String, DtpKnowledgeInfoCodeDTO>> result = new LinkedHashMap<>();
        for (Map.Entry<String, List<DtpKnowledgeInfoCodeDTO>> entry : classCodeAndInfoCodes.entrySet()) {
            String classCode = entry.getKey();
            List<DtpKnowledgeInfoCodeDTO> infoCodeDefines = entry.getValue();
            if (CollectionUtils.isEmpty(infoCodeDefines)) {
                continue;
            }
            Map<String, DtpKnowledgeInfoCodeDTO> infoCodeAndDefine = infoCodeDefines.stream()
                    .collect(Collectors.toMap(DtpKnowledgeInfoCodeDTO::getCode, Function.identity(), (o1, o2) -> o1));
            result.put(classCode, infoCodeAndDefine);
        }
        return result;
    }

    @Override
    public Map<String, Map<String, DtpKnowledgeClassCodeTagResponse>> mapClassCodeTypeAndTag(
            @NonNull DtpKnowledgeClassCodeTagQueryParam param) {
        List<DtpKnowledgeClassCodeTagResponse> responses = this.listClassCodeTags(param);
        if (CollectionUtils.isEmpty(responses)) {
            return Collections.emptyMap();
        }
        Map<String, Map<String, DtpKnowledgeClassCodeTagResponse>> result = new LinkedHashMap<>();
        for (DtpKnowledgeClassCodeTagResponse response : responses) {
            MapExecutor.dynamicPut(result, response.getClassCode(), response.getTagType(), response, LinkedHashMap::new);
        }
        return result;
    }

    @Override
    public Map<String, String> mapTypeCodeAndName(@NonNull DtpKnowledgeTypeCodeEnum typeCode) {
        return this.listTypeDefines(typeCode).stream().collect(Collectors.toMap(DtpKnowledgeTypeResponse::getCode,
                DtpKnowledgeTypeResponse::getName, (o1, o2) -> o1));
    }

    @Override
    public List<JSONObject> listTypeByCode(List<String> codes) {
        if (CollectionUtil.isEmpty(codes)) {
            return null;
        }
        R<List<JSONObject>> typeByCode = this.dtpKnowledgeServerClient.listTypeByCode(codes);
        if (typeByCode != null && typeByCode.getIsSuccess()) {
            return typeByCode.getData();
        }
        return null;
    }

    @Override
    @Cacheable(cacheNames = CacheKey.Dynamic.KNOWLEDGE_CLASS_CODE_NAME_MAP, key = "#objectType", condition = "#queryCache"
            , unless = "#result == null || #result.isEmpty()")
    public Map<String, String> mapClassCodeAndNameByType(boolean queryCache, boolean isThrows, @NonNull DtpRwdObjectTypeEnum objectType) {
        List<DtpKnowledgeObjectInfoCodeResponse> responseList = query(isThrows, null, null, LocaleMessageUtil.getMessage("key_3c2cdda3d46247b485dc189fd53e1caa"), objectType,
                () -> dtpKnowledgeServerClient.listObjectClassByType(objectType), ArrayList::new);
        return responseList.stream().collect(Collectors.toMap(DtpKnowledgeObjectInfoCodeResponse::getCode,
                DtpKnowledgeObjectInfoCodeResponse::getName, (v1, v2) -> v2));
    }

    @Override
    public Logger log() {
        return log;
    }

    @Override
    public String logTag() {
        return LOG_TAG;
    }
}
