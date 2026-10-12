package com.persagy.apccontrol.basic.structure.infocodeconfig.lifecycle;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.infocodeconfig.InfoCodeConfig;
import com.persagy.apccontrol.basic.model.entity.infocodeconfig.InfoCodeGroup;
import com.persagy.apccontrol.basic.model.entity.infocodeconfig.InfoCodeTemplateType;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtpknowledge.DtpKnowledgeTypeCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.knowledge.DtpKnowledgeInfoCodeByTagQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.knowledge.DtpKnowledgeInfoCodeTagResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.knowledge.DtpKnowledgeTypeResponse;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param.InfoCodeInitByObjectParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeTemplateTypeModel;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 信息点聚合根生命周期日志回调
 *
 * @author JiYinchuan
 * @since 5.240801.0
 */
@Component()
@Scope("prototype")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InfoCodeAggregateLifeTagAware implements InfoCodeAggregateLifeAware, LogComponent {

    private static final Level LOG_LEVEL = Level.TRACE;
    private final DtpKnowledgeClientService dtpKnowledgeClientService;
    private static final String OTHER_GROUP_CODE = "other";
    private static final Integer DEFAULT_SEQUENCE = 99999;

    //支持的系统
    private static final List<RunningMonitorTypeEnum> RUNNING_MONITOR_TYPES = Arrays.asList(
            RunningMonitorTypeEnum.WATER_SUPPLY,
            RunningMonitorTypeEnum.AIR_CONDITIONING_C_H_SOURCE
    );

    //支持的类型和标签
    private static final Map<InfoCodeTemplateTypeEnum,List<String>> TEMPLATE_TYPES_TAG = new HashMap<>();
    //标签对应的信息点类型
    private static final Map<String,InfoCodeTypeEnum> TAG_INFO_CODE_TYPE = new HashMap<>();

    private Long templateIdGlobal;

    static {
        TEMPLATE_TYPES_TAG.put(InfoCodeTemplateTypeEnum.DETAIL,Arrays.asList("3","5"));
        TAG_INFO_CODE_TYPE.put("3",InfoCodeTypeEnum.IOT_RUNNING);
        TAG_INFO_CODE_TYPE.put("5",InfoCodeTypeEnum.BASIC);
    }

    //一二级标签关系
    private final Set<String> x1TagsGlobal = new HashSet<>();
    private final Map<String,String> x2TagMapGlobal = new HashMap<>();

    //检查满足条件
    private boolean check(@NonNull InfoCodeInitByObjectParam param){
        if(CollectionUtil.isEmpty(param.getObjects())){
            return true;
        }
        RunningMonitorTypeEnum runningMonitorType = param.getRunningMonitorType();
        if(!RUNNING_MONITOR_TYPES.contains(runningMonitorType)){
            return true;
        }

        InfoCodeTemplateTypeEnum templateTypeEnum = param.getTemplateTypes().iterator().next();
        return !TEMPLATE_TYPES_TAG.containsKey(templateTypeEnum);
    }
    @Override
    public Collection<InfoCodeTemplateTypeModel> handleTemplateTypes( @NonNull InfoCodeInitByObjectParam param,@Nullable Collection<InfoCodeTemplateTypeModel> templateTypeModels) {
        if(check(param)){
            return templateTypeModels;
        }

        if(CollectionUtil.isNotEmpty(templateTypeModels)){
            templateIdGlobal = templateTypeModels.iterator().next().getTemplateTypeId();
            return templateTypeModels;
        }

        InfoCodeTemplateTypeEnum templateTypeEnum = param.getTemplateTypes().iterator().next();
        // Get the first object from the collection and extract its class code
        DtpRwdObjectDTO firstObject = param.getObjects().iterator().next();
        InfoCodeTemplateType templateType = new InfoCodeTemplateType();
        templateIdGlobal = IdWorker.getId();
        templateType.setId(templateIdGlobal);
        templateType.setRunningMonitorType(param.getRunningMonitorType());
        templateType.setRunningMonitorSecondType(param.getRunningMonitorSecondType());
        templateType.setTemplateType(templateTypeEnum);
        templateType.setObjectType(DtpRwdObjectTypeEnum.analyze(firstObject.getObjType()));
        templateType.setClassCode(firstObject.getClassCode());
        List<InfoCodeTemplateTypeModel> newTemplateTypeModels = InfoCodeTemplateTypeModel.from(Collections.singletonList(templateType));
        if(templateTypeModels == null){
            return newTemplateTypeModels;
        }
        templateTypeModels.addAll(newTemplateTypeModels);
        return templateTypeModels;
    }

    /**
     * 构建标签代码
     * @param x1Tag 一级标签
     * @param x2Tag 二级标签，可能为null
     * @return 标签代码字符串
     */
    private String buildTagCode(String x1Tag, String x2Tag) {
        StringBuilder tagCode = new StringBuilder();
        tagCode.append(DtpKnowledgeTypeCodeEnum.X1_TAG.value()).append("_").append(x1Tag);
        if (x2Tag != null) {
            tagCode.append("_").append(DtpKnowledgeTypeCodeEnum.X2_TAG.value()).append("_").append(x2Tag);
        }
        return tagCode.toString();
    }

    @Override
    public List<InfoCodeConfig> handleInfoCodeConfigs(
            @NonNull InfoCodeInitByObjectParam param,
            @Nullable List<InfoCodeConfig> infoCodeConfigs) {
        if(check(param)){
            return infoCodeConfigs;
        }

        // 获取所有objects的classCode集合
        Set<String> classCodes = param.getObjects().stream()
                .map(DtpRwdObjectDTO::getClassCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if(infoCodeConfigs == null){
            infoCodeConfigs = new ArrayList<>();
        }
        List<InfoCodeConfig> finalInfoCodeConfigs1 = infoCodeConfigs;
        classCodes.stream().forEach(classCode -> {
        //String classCode = firstObject.getClassCode();
        InfoCodeTemplateTypeEnum templateTypeEnum = param.getTemplateTypes().iterator().next();
        DtpKnowledgeInfoCodeByTagQueryParam.TagParam tagParam = new DtpKnowledgeInfoCodeByTagQueryParam.TagParam();
        tagParam.setParamType(0);
        tagParam.setTypeCode(DtpKnowledgeTypeCodeEnum.X1_TAG.getValue());
        tagParam.setTagCode(TEMPLATE_TYPES_TAG.get(templateTypeEnum));

        DtpKnowledgeInfoCodeByTagQueryParam tagQueryParam = new DtpKnowledgeInfoCodeByTagQueryParam();
        tagQueryParam.setClassCode(Collections.singletonList(classCode));
        tagQueryParam.setTagParams(Collections.singletonList(tagParam));
        Map<String, List<DtpKnowledgeInfoCodeTagResponse>> tagListMap = dtpKnowledgeClientService.listInfoByTag(tagQueryParam);
        if(MapUtils.isEmpty(tagListMap) || tagListMap.get(classCode) == null) {
            return;
        }

        List<InfoCodeConfig> finalInfoCodeConfigs = finalInfoCodeConfigs1;
        tagListMap.get(classCode).forEach(item -> {
            // 添加信息点
            Map<String, List<String>> tags = item.getTags();
            String x1Tag = tags.get(DtpKnowledgeTypeCodeEnum.X1_TAG.value()).get(0);
            x1TagsGlobal.add(x1Tag);
            
            String x2Tag = null;
            if (tags.containsKey(DtpKnowledgeTypeCodeEnum.X2_TAG.value())) {
                x2Tag = tags.get(DtpKnowledgeTypeCodeEnum.X2_TAG.value()).get(0);
                x2TagMapGlobal.put(x2Tag, x1Tag);
            }else{
                x2Tag = OTHER_GROUP_CODE;
            }
            
            // 构建标签代码
            String tagCode = buildTagCode(x1Tag, x2Tag);

            InfoCodeConfig newInfoCodeConfig = new InfoCodeConfig();
            newInfoCodeConfig.setInfoCode(item.getCode());
            newInfoCodeConfig.setInfoCodeName(item.getName());
            newInfoCodeConfig.setInfoCodeType(TAG_INFO_CODE_TYPE.get(x1Tag));

            // 设置序列号
            if (tags.containsKey("infosorting")) {
                newInfoCodeConfig.setSequence(Integer.valueOf(tags.get("infosorting").get(0)));
            } else {
                newInfoCodeConfig.setSequence(DEFAULT_SEQUENCE);
            }

            newInfoCodeConfig.setRelGroupCode(tagCode);
            newInfoCodeConfig.setRelTemplateTypeId(templateIdGlobal);
            if(newInfoCodeConfig.getInfoCodeType() != null) {
                finalInfoCodeConfigs.add(newInfoCodeConfig);
            }
        });
        });

        return infoCodeConfigs;
    }

    @Override
    public List<InfoCodeGroup> handleInfoCodeGroups(
            @NonNull InfoCodeInitByObjectParam param,
            @Nullable List<InfoCodeGroup> infoCodeGroups) {
        if(check(param)){
            return infoCodeGroups;
        }

        if(infoCodeGroups == null){
            infoCodeGroups = new ArrayList<>();
        }

        List<DtpKnowledgeTypeResponse> dtpKnowledgeTypeResponses = dtpKnowledgeClientService.listTypeDefines(DtpKnowledgeTypeCodeEnum.X1_TAG);
        Map<String, DtpKnowledgeTypeResponse> x1TagMap = dtpKnowledgeTypeResponses.stream().collect(Collectors.toMap(
                DtpKnowledgeTypeResponse::getCode, Function.identity(), (o1, o2) -> o1));

        dtpKnowledgeTypeResponses = dtpKnowledgeClientService.listTypeDefines(DtpKnowledgeTypeCodeEnum.X2_TAG);
        Map<String, DtpKnowledgeTypeResponse> x2TagMap = dtpKnowledgeTypeResponses.stream().collect(Collectors.toMap(
                DtpKnowledgeTypeResponse::getCode, Function.identity(), (o1, o2) -> o1));

        //添加一级分组
        Map<String,Long> x1TagIdMap = new HashMap<>();
        for (String tag : x1TagsGlobal) {
            DtpKnowledgeTypeResponse dtpKnowledgeTypeResponse = x1TagMap.get(tag);
            if (dtpKnowledgeTypeResponse == null) {
                continue;
            }
            
            InfoCodeGroup newInfoCodeGroup = new InfoCodeGroup();
            newInfoCodeGroup.setGroupCode(buildTagCode(tag, null));
            newInfoCodeGroup.setGroupName(dtpKnowledgeTypeResponse.getName());
            newInfoCodeGroup.setSequence(dtpKnowledgeTypeResponse.getShowOrder());
            newInfoCodeGroup.setId(IdWorker.getId());
            x1TagIdMap.put(tag, newInfoCodeGroup.getId());
            infoCodeGroups.add(newInfoCodeGroup);

            //添加默认的二级分组
            InfoCodeGroup otherInfoCodeGroup = new InfoCodeGroup();
            otherInfoCodeGroup.setGroupCode(buildTagCode(tag, OTHER_GROUP_CODE));
            otherInfoCodeGroup.setGroupName(LocaleMessageUtil.getMessage("key_other_group_name"));
            otherInfoCodeGroup.setSequence(DEFAULT_SEQUENCE);
            otherInfoCodeGroup.setId(IdWorker.getId());
            otherInfoCodeGroup.setRelParentId(newInfoCodeGroup.getId());
            infoCodeGroups.add(otherInfoCodeGroup);
        }

        //添加二级分组
        for (Map.Entry<String, String> entry : x2TagMapGlobal.entrySet()) {
            String x2Tag = entry.getKey();
            String x1Tag = entry.getValue();
            
            DtpKnowledgeTypeResponse dtpKnowledgeTypeResponse = x2TagMap.get(x2Tag);
            if (dtpKnowledgeTypeResponse == null) {
                continue;
            }
            
            Long parentId = x1TagIdMap.get(x1Tag);
            if (parentId == null) {
                continue;
            }
            
            InfoCodeGroup newInfoCodeGroup = new InfoCodeGroup();
            newInfoCodeGroup.setGroupCode(buildTagCode(x1Tag, x2Tag));
            newInfoCodeGroup.setGroupName(dtpKnowledgeTypeResponse.getName());
            newInfoCodeGroup.setSequence(dtpKnowledgeTypeResponse.getShowOrder());
            newInfoCodeGroup.setId(IdWorker.getId());
            newInfoCodeGroup.setRelParentId(parentId);
            infoCodeGroups.add(newInfoCodeGroup);
        }

        return infoCodeGroups;
    }

    @Override
    public String logTag() {
        return "信息点聚合根生命周期标签处理-日志";
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
