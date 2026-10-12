package com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param;

import cn.hutool.extra.spring.SpringUtil;
import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdCriteriaSignEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.page.DefaultListPage;
import com.persagy.apccontrol.basic.model.page.Pageable;
import com.persagy.apccontrol.basic.model.page.query.PageQuery;
import com.persagy.apccontrol.basic.model.param.infocodeconfig.InfoCodeStaticFilter;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectOrderParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.knowledge.DtpKnowledgeObjectClassCodeResponse;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.InfoCodeGateway;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeTemplateTypeModel;
import com.persagy.apccontrol.basic.structure.performance.PerformanceTracker;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ValidationException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 信息点配置-对象查询
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Data
public class InfoCodeObjectQueryParam implements BaseCheckParamProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfoCodeObjectQueryParam.class);
    private static final String LOG_TAG = "信息点配置-对象查询";
    private static final DtpRwdClientService dtpRwdClientService = SpringUtil.getBean(DtpRwdClientService.class);
    private static final DtpKnowledgeClientService dtpKnowledgeClientService = SpringUtil.getBean(DtpKnowledgeClientService.class);

    /**
     * 分页查询
     */
    private PageQuery pageQuery;

    /**
     * 运行监测类型
     */
    private RunningMonitorTypeEnum runningMonitorType;

    /**
     * 运行监测二级类型
     */

    private @Nullable RunningMonitorSecondTypeEnum runningMonitorSecondType;

    /**
     * 模版ID
     */
    private Long templateTypeId;

    /**
     * 模版ID
     */
    private Set<Long> templateTypeIds;

    /**
     * 模版类型
     */
    private Collection<InfoCodeTemplateTypeEnum> templateTypes;

    /**
     * 模版类型模型
     */
    private Collection<InfoCodeTemplateTypeModel> templateTypeModels;

    /**
     * 设备类编码
     */
    private String classCode;

    /**
     * 设备类编码
     */
    private Collection<String> classCodes;

    /**
     * 排除classCode
     */
    private Collection<String> excludeClassCodes;

    /**
     * 对象类型
     */
    private DtpRwdObjectTypeEnum objectType;

    /**
     * 对象类型列表
     */
    private Collection<DtpRwdObjectTypeEnum> objectTypes;

    /**
     * 是否过滤管控对象
     */
    private Boolean filterControlObject;

    /**
     * 过滤对象ID, 为空时不过滤
     */
    private Set<String> filterObjectIds;

    /**
     * 过滤所在系统ID, 为空时不过滤
     */
    private Set<String> filterInSystemIds;

    /**
     * 对象名称, 为空时查询所有
     */
    private String localName;

    /**
     * 静态信息点过滤, 为空时查询所有
     */
    private List<InfoCodeStaticFilter> staticInfoCodeFilters;

    /**
     * 标识和静态信息点过滤, 为空时查询所有
     */
    private Map<DtpRwdCriteriaSignEnum, List<InfoCodeStaticFilter>> signAndStaticInfoCodeFilters;

    /**
     * 服务空间ID, 为空时查询所有
     */
    private Set<String> serviceSpaceIds;

    /**
     * 所在空间ID, 为空时查询所有
     */
    private Set<String> inSpaceIds;

    /**
     * 排序参数, 为空时不排序
     */
    private List<DtpRwdObjectOrderParam> orders;

    /**
     * 过滤对象
     *
     * @param objects 对象
     * @return 过滤后的对象
     * @since 7.250802.0
     */
    public List<DtpRwdObjectDTO> filterObjects(@NonNull List<DtpRwdObjectDTO> objects) {
        if (CollectionUtils.isEmpty(objects)) {
            return objects;
        }
        if (CollectionUtils.isNotEmpty(this.filterObjectIds)) {
            objects.removeIf(object -> !filterObjectIds.contains(object.getId()));
            if (CollectionUtils.isEmpty(objects)) {
                return objects;
            }
        }
        objects = this.filterObjectsInSystems(objects);
        if (CollectionUtils.isEmpty(objects)) {
            return objects;
        }
        Set<String> objectIds = objects.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        List<Consumer<DtpRwdObjectQueryParam>> relationQueryCallbacks = new ArrayList<>();
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        if (StringUtils.isNotBlank(this.localName)) {
            queryParam.addInfoCode(DtpRwdObjectDTO.Fields.localName, DtpRwdCriteriaSignEnum.LIKE, localName);
        }
        if (CollectionUtils.isNotEmpty(this.staticInfoCodeFilters)) {
            queryParam.addInfoCode(this.staticInfoCodeFilters);
        }
        if (MapUtils.isNotEmpty(this.signAndStaticInfoCodeFilters)) {
            queryParam.addInfoCode(this.signAndStaticInfoCodeFilters);
        }
        if (CollectionUtils.isNotEmpty(this.serviceSpaceIds)) {
            relationQueryCallbacks.add(param -> param.setRelationFrom(
                    new DtpRwdObjectRelationQueryParam(DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE)
                            .setObjTo(this.serviceSpaceIds)));
        }
        if (CollectionUtils.isNotEmpty(this.inSpaceIds)) {
            relationQueryCallbacks.add(param -> param.setRelationFrom(
                    new DtpRwdObjectRelationQueryParam(DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE)
                            .setObjTo(this.inSpaceIds)));
        }
        if (CollectionUtils.isNotEmpty(relationQueryCallbacks)) {
            Iterator<Consumer<DtpRwdObjectQueryParam>> iterator = relationQueryCallbacks.iterator();
            while (iterator.hasNext()) {
                Consumer<DtpRwdObjectQueryParam> relationQueryCallback = iterator.next();
                // 不存在关联参数时, 将第一个添加到查询条件并从列表中移除
                if (!queryParam.containsRelationParam()) {
                    relationQueryCallback.accept(queryParam);
                    iterator.remove();
                    break;
                }
            }
        }
        List<DtpRwdObjectDTO> finalObjects = objects;
        if (!queryParam.invalid()) {
            queryParam.setId(objectIds);
            queryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
            Set<String> validObjectIds = dtpRwdClientService.listObject(queryParam).stream()
                    .map(DtpRwdObjectDTO::getId)
                    .collect(Collectors.toSet());
            // 处理剩下的关联关系处理, 对每次匹配的对象取交集
            if (CollectionUtils.isNotEmpty(relationQueryCallbacks)) {
                for (Consumer<DtpRwdObjectQueryParam> relationQueryCallback : relationQueryCallbacks) {
                    queryParam.setRelationFrom(null);
                    queryParam.setRelationTo(null);
                    relationQueryCallback.accept(queryParam);
                    Set<String> eachValidObjectIds = dtpRwdClientService.listObject(queryParam).stream()
                            .map(DtpRwdObjectDTO::getId)
                            .collect(Collectors.toSet());
                    validObjectIds = SetUtils.intersection(validObjectIds, eachValidObjectIds);
                }
            }
            if (CollectionUtils.isNotEmpty(validObjectIds)) {
                finalObjects = new ArrayList<>();
                for (DtpRwdObjectDTO object : objects) {
                    if (validObjectIds.contains(object.getId())) {
                        finalObjects.add(object);
                    }
                }
            } else {
                finalObjects = Collections.emptyList();
            }
        }
        Comparator<DtpRwdObjectDTO> comparator = DtpRwdObjectOrderParam.toObjectComparator(this.orders, finalObjects,
                Arrays.stream(InfoCodeGateway.listObjectRequiredColumns()).collect(Collectors.toSet()));
        if (Objects.nonNull(comparator)) {
            finalObjects.sort(comparator);
        }
        if (Objects.nonNull(this.pageQuery)) {
            Pageable<List<DtpRwdObjectDTO>> pageable = new DefaultListPage<>(
                    this.pageQuery.getPageNo(), this.pageQuery.getPageSize(),
                    finalObjects.size(), finalObjects);
            int toIndex = Math.min(pageable.getOffset() + pageable.getPageSize(), finalObjects.size());
            int fromIndex = Math.min(pageable.getOffset(), toIndex);
            finalObjects = finalObjects.subList(fromIndex, toIndex);
        }
        return finalObjects;
    }

    /**
     * 按对象类型过滤对象所在系统.
     *
     * @param objects 对象
     * @return 过滤后的对象
     * @since 7.260601.0
     */
    private List<DtpRwdObjectDTO> filterObjectsInSystems(@NonNull List<DtpRwdObjectDTO> objects) {
        if (CollectionUtils.isEmpty(objects) || CollectionUtils.isEmpty(this.filterInSystemIds)) {
            return objects;
        }
        Set<String> classCodes = objects.stream()
                .map(DtpRwdObjectDTO::getClassCode)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        Map<String, String> classCodeAndObjType = Collections.emptyMap();
        if (CollectionUtils.isNotEmpty(classCodes)) {
            PerformanceTracker tracker = PerformanceTracker.currentOrDetached("查询对象类型定义");
            try (PerformanceTracker.SafeNode safeNode =
                         tracker.track("DtpKnowledgeClientService#listClassCodes")) {
                classCodeAndObjType = dtpKnowledgeClientService.listClassCodes(classCodes).stream()
                        .collect(Collectors.toMap(DtpKnowledgeObjectClassCodeResponse::getCode,
                                DtpKnowledgeObjectClassCodeResponse::getObjType, (o1, o2) -> o1));
                safeNode.recordStep("查询知识库设备类定义");
            }
        }

        Map<DtpRwdRelationTypeEnum, Set<String>> relationTypeAndObjectIds = new LinkedHashMap<>();
        for (DtpRwdObjectDTO object : objects) {
            String objectId = object.getId();
            if (StringUtils.isBlank(objectId)) {
                continue;
            }
            String objType = classCodeAndObjType.get(object.getClassCode());
            if (StringUtils.isBlank(objType)) {
                objType = object.getObjType();
            }
            DtpRwdRelationTypeEnum relationType = toSystemRelationType(objType);
            if (Objects.isNull(relationType)) {
                continue;
            }
            relationTypeAndObjectIds.computeIfAbsent(relationType, item ->
                    new LinkedHashSet<>()).add(objectId);
        }
        if (MapUtils.isEmpty(relationTypeAndObjectIds)) {
            return Collections.emptyList();
        }

        Set<String> validObjectIds = new LinkedHashSet<>();
        PerformanceTracker tracker = PerformanceTracker.currentOrDetached("查询对象所在系统");
        try (PerformanceTracker.SafeNode safeNode = tracker.track("DtpRwdClientService#listObject")) {
            for (Map.Entry<DtpRwdRelationTypeEnum, Set<String>> entry : relationTypeAndObjectIds.entrySet()) {
                DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
                queryParam.setId(entry.getValue());
                queryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
                queryParam.setRelationTo(new DtpRwdObjectRelationQueryParam(entry.getKey())
                        .setObjFrom(this.filterInSystemIds));
                validObjectIds.addAll(dtpRwdClientService.listObject(queryParam).stream()
                        .map(DtpRwdObjectDTO::getId)
                        .collect(Collectors.toSet()));
                safeNode.recordStep(String.format("查询RWD所在系统对象[%s]", entry.getKey()));
            }
        }
        if (CollectionUtils.isEmpty(validObjectIds)) {
            return Collections.emptyList();
        }
        return objects.stream()
                .filter(object -> validObjectIds.contains(object.getId()))
                .collect(Collectors.toList());
    }

    /**
     * 根据对象类型获取所在系统关系类型.
     *
     * @param objType 对象类型
     * @return 所在系统关系类型
     * @since 7.260601.0
     */
    private DtpRwdRelationTypeEnum toSystemRelationType(String objType) {
        if (StringUtils.equals(objType, DtpRwdObjectTypeEnum.SPACE.value())) {
            return DtpRwdRelationTypeEnum.SYSTEM_SERVICE_SPACE;
        }
        if (StringUtils.equals(objType, DtpRwdObjectTypeEnum.EQUIPMENT.value())) {
            return DtpRwdRelationTypeEnum.EQ_IN_SYSTEM;
        }
        if (StringUtils.equals(objType, DtpRwdObjectTypeEnum.EQUIPMENT_GROUP.value())) {
            return DtpRwdRelationTypeEnum.EQUIP_GROUP_IN_SYSTEM;
        }
        return null;
    }

    @Override
    public void checkAndSet() {
        if (this.checkAssembleCollectionNull(this.templateTypeId, this.templateTypeIds)
                && Objects.isNull(this.runningMonitorType)) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_1b6c59a8b466464194bb1df936b59801"));
        }
    }
}
