package com.persagy.apccontrol.basic.model.context.infocodeconfig;

import cn.hutool.extra.spring.SpringUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.config.cache.provider.redis.RedisCacheTemplate;
import com.persagy.apccontrol.basic.convert.BaseConvert;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdBuildingDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFloorDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.infocodeconfig.InfoCodeTemplateType;
import com.persagy.apccontrol.basic.model.page.DefaultListPage;
import com.persagy.apccontrol.basic.model.page.query.PageQuery;
import com.persagy.apccontrol.basic.model.param.infocodeconfig.DynamicInfoCodeFilter;
import com.persagy.apccontrol.basic.model.param.infocodeconfig.InfoCodeConfigByTemplateIdQueryParam;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectOrderParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.infocodeconfig.InfoCodeTemplateTypeResource;
import com.persagy.apccontrol.basic.structure.infocodeconfig.converter.InfoCodeConverter;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.InfoCodeGateway;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param.InfoCodeInitByObjectParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param.InfoCodeObjectQueryParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.lifecycle.InfoCodeAggregateLifeAware;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeTemplateTypeModel;
import com.persagy.apccontrol.basic.structure.performance.PerformanceTracker;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.framework.common.exception.enums.ErrorCodeConstants;
import com.persagy.validate.utility.enumeration.error.client.RequestServerErrorEnum;
import com.persagy.validate.utility.exception.ApplicationException;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.text.Collator;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 信息点配置-配置查询上下文
 *
 * @author 薛涛
 * @since 5.240602.0
 */
@Data
public class InfoCodeConfigQueryContext implements LogComponent {

    private final int partitionSize = 100;

    // ------------------------- 入参 -------------------------

    /**
     * 初始化参数
     */
    private final @NonNull InfoCodeConfigByTemplateIdQueryParam param;

    /**
     * 执行次数, 默认为 {@literal 1}
     */
    private int executeCount = 1;

    private final InfoCodeConverter infoCodeConverter = SpringUtil.getBean(InfoCodeConverter.class);
    private final InfoCodeTemplateTypeResource infoCodeTemplateTypeResource = SpringUtil.getBean(InfoCodeTemplateTypeResource.class);
    private final InfoCodeGateway infoCodeGateway = SpringUtil.getBean(InfoCodeGateway.class);
    private final DtpRwdClientService dtpRwdClientService = SpringUtil.getBean(DtpRwdClientService.class);
    private final AbstractIotDataCacheServer abstractIotDataCacheServer = SpringUtil.getBean(AbstractIotDataCacheServer.class);
    private final ObjectProvider<InfoCodeAggregateLifeAware> infoCodeAggregateLifeAware =
            SpringUtil.getBeanFactory().getBeanProvider(InfoCodeAggregateLifeAware.class);
    private final RedisCacheTemplate redisCacheTemplate = SpringUtil.getBean(RedisCacheTemplate.class);

    // ------------------------- 过程 -------------------------

    /**
     * 模版类型
     */
    private InfoCodeTemplateTypeModel templateTypeModel;

    /**
     * 对象列表
     */
    private List<DtpRwdObjectDTO> objects = Collections.emptyList();

    /**
     * 对象数量
     */
    private Integer objectCount = -1;

    /**
     * 聚合根
     */
    private List<InfoCodeAggregate> aggregates = new ArrayList<>();

    // ------------------------- 结果 -------------------------

    /**
     * 是否失败
     */
    private boolean failed = false;

    // ------------------------- 行为 -------------------------

    /**
     * 转换初始化聚合根参数
     *
     * @return 初始化聚合根参数
     * @since 5.240602.0
     */
    public InfoCodeInitByObjectParam toEquipmentInfoCodeInitByObjectParam() {
        if (Objects.isNull(this.templateTypeModel)) {
            throw new BusinessException(ErrorCodeConstants.SYSTEM_ERROR_B0001.getCode(), LocaleMessageUtil.getMessage("key_8356b8ff6af845ad99d6033c92e55c61"));
        }
        if (CollectionUtils.isEmpty(this.objects)) {
            throw new BusinessException(ErrorCodeConstants.SYSTEM_ERROR_B0001.getCode(), LocaleMessageUtil.getMessage("key_b766020813bb4a0a913bd011d8e6b97c"));
        }
        PageQuery page = this.param.getPage();
        InfoCodeInitByObjectParam result = new InfoCodeInitByObjectParam();
        result.setRunningMonitorType(this.templateTypeModel.getRunningMonitorType());
        result.setRunningMonitorSecondType(this.templateTypeModel.getRunningMonitorSecondType());
        result.setPage(Objects.nonNull(page) ? new DefaultListPage<>(page) : null);
        result.setTemplateTypeIds(Sets.newHashSet(this.templateTypeModel.getTemplateTypeId()));
        result.setTemplateTypes(Lists.newArrayList(this.templateTypeModel.getTemplateType()));
        result.setTemplateTypeModels(Lists.newArrayList(this.templateTypeModel));
        result.setForceMathTemplate(true);
        result.setObjects(this.objects);
        result.setGroupCodes(this.param.getGroupCodes());
        result.setFilterGroups(this.param.getFilterGroups());
        result.setExcludeGroups(this.param.getExcludeGroups());
        result.setQueryIotInfoCodeRule(this.param.getQueryIotInfoCodeRule());
        result.setNotFilterIotInfoCodeRule(this.param.getNotFilterIotInfoCodeRule());
        result.setFilterBothNotExistIotInfoCodeRule(this.param.getFilterBothNotExistIotInfoCodeRule());
        return result;
    }

    /**
     * 填充模版类型
     *
     * @since 5.240602.0
     */
    public void fillTemplateType() {
        PerformanceTracker tracker = PerformanceTracker.currentOrDetached("填充模版类型");
        try (PerformanceTracker.SafeNode safeNode =
                     tracker.track("InfoCodeConfigQueryContext#fillTemplateType")) {
            Optional<InfoCodeTemplateType> templateTypeOpt =
                    infoCodeTemplateTypeResource.get(param.getTemplateTypeId());
            safeNode.recordStep("查询模版类型配置");
            if (templateTypeOpt.isPresent()) {
                this.templateTypeModel = infoCodeConverter.toTemplateTypeModel(templateTypeOpt.get());
            } else {
                this.failed = true;
            }
            safeNode.recordStep("转换模版类型模型");
        }
    }

    /**
     * 填充物理世界对象
     *
     * @since 5.240602.0
     */
    public void fillRwdObjects() {
        Boolean filterControlObject = this.param.getFilterControlObject();
        if (Objects.isNull(this.templateTypeModel)) {
            throw new BusinessException(ErrorCodeConstants.SYSTEM_ERROR_B0001.getCode(), LocaleMessageUtil.getMessage("key_8356b8ff6af845ad99d6033c92e55c61"));
        }
        PerformanceTracker tracker = PerformanceTracker.currentOrDetached("填充物理世界对象");
        try (PerformanceTracker.SafeNode safeNode =
                     tracker.track("InfoCodeConfigQueryContext#fillRwdObjects")) {
            InfoCodeObjectQueryParam infoCodeObjectQueryParam = new InfoCodeObjectQueryParam();
            infoCodeObjectQueryParam.setRunningMonitorType(this.templateTypeModel.getRunningMonitorType());
            infoCodeObjectQueryParam.setRunningMonitorSecondType(this.templateTypeModel.getRunningMonitorSecondType());
            infoCodeObjectQueryParam.setTemplateTypeModels(Lists.newArrayList(this.templateTypeModel));
            infoCodeObjectQueryParam.setFilterControlObject(filterControlObject);
            infoCodeObjectQueryParam.setFilterObjectIds(this.param.getFilterObjectIds());
            infoCodeObjectQueryParam.setFilterInSystemIds(this.param.getFilterInSystemIds());
            infoCodeObjectQueryParam.setLocalName(this.param.getLocalName());
            infoCodeObjectQueryParam.setStaticInfoCodeFilters(this.param.getStaticInfoCodeFilters());
            infoCodeObjectQueryParam.setSignAndStaticInfoCodeFilters(this.param.getSignAndStaticInfoCodeFilters());
            infoCodeObjectQueryParam.setServiceSpaceIds(this.param.getServiceSpaceIds());
            infoCodeObjectQueryParam.setInSpaceIds(this.param.getInSpaceIds());
            infoCodeObjectQueryParam.setOrders(this.param.getOrders());
            safeNode.recordStep("准备对象查询参数");
            List<DtpRwdObjectDTO> objects = infoCodeGateway.listTemplateTypeObjects(infoCodeObjectQueryParam);
            safeNode.recordStep("查询模版关联对象");
            if (CollectionUtils.isNotEmpty(objects)) {
                this.objects = objects;
                this.objectCount = objects.size();
            } else {
                this.failed = true;
                logProjectInstance().warn("未查询到模版下对象");
            }
            safeNode.recordStep("处理对象查询结果");
        }
    }

    /**
     * 过滤动态信息点
     *
     * @since 7.250601.0
     */
    @SuppressWarnings("unchecked")
    public void filterDynamicInfoCode() {
        List<DynamicInfoCodeFilter> dynamicInfoCodeFilters = this.param.getDynamicInfoCodeFilters();
        if (CollectionUtils.isEmpty(this.objects)) {
            this.failed = true;
            return;
        }
        if (CollectionUtils.isEmpty(dynamicInfoCodeFilters)) {
            return;
        }
        PerformanceTracker tracker = PerformanceTracker.currentOrDetached("过滤动态信息点");
        try (PerformanceTracker.SafeNode safeNode =
                     tracker.track("InfoCodeConfigQueryContext#filterDynamicInfoCode")) {
            // 信息点编码 -> 信息点筛选条件
            Map<String, DynamicInfoCodeFilter> infoCodeAndFilter = dynamicInfoCodeFilters.stream()
                    .collect(Collectors.toMap(DynamicInfoCodeFilter::getInfoCode, Function.identity(),
                            (o1, o2) -> o1));

            // 对象ID -> 对象
            Map<String, DtpRwdObjectDTO> objectIdAndData = this.objects.stream().collect(Collectors.toMap(
                    DtpRwdObjectDTO::getId, Function.identity(), (o1, o2) -> o1));
            // 查询信息点值
            List<InfoCodeValueVO> infoCodeValues = abstractIotDataCacheServer.listInfoCodeValueInOneHour(
                    objectIdAndData.keySet(), infoCodeAndFilter.keySet());
            safeNode.recordStep("查询IOT动态信息点值");
            MultiKeyMap<String, InfoCodeValueVO> objectIdInfoCodeAndValue = new MultiKeyMap<>();
            for (InfoCodeValueVO infoCodeValue : infoCodeValues) {
                MultiKey<String> multiKey = new MultiKey<>(infoCodeValue.getObjectId(), infoCodeValue.getInfoCode());
                objectIdInfoCodeAndValue.put(multiKey, infoCodeValue);
            }
            Iterator<DtpRwdObjectDTO> iterator = this.objects.iterator();
            while (iterator.hasNext()) {
                DtpRwdObjectDTO object = iterator.next();
                String objectId = object.getId();
                for (DynamicInfoCodeFilter dynamicInfoCodeFilter : dynamicInfoCodeFilters) {
                    String infoCode = dynamicInfoCodeFilter.getInfoCode();

                    InfoCodeValueVO infoCodeValue = objectIdInfoCodeAndValue.get(objectId, infoCode);
                    if (!dynamicInfoCodeFilter.equalsDataValue(infoCodeValue)) {
                        iterator.remove();
                        break;
                    }
                }
            }
            this.objectCount = this.objects.size();
            if (CollectionUtils.isEmpty(this.objects)) {
                this.failed = true;
            }
            safeNode.recordStep("过滤对象");
        }
    }

    /**
     * 填充聚合根
     *
     * @since 5.240602.0
     */
    public void fillAggregates() {
        PerformanceTracker tracker = PerformanceTracker.currentOrDetached("填充聚合根");
        try (PerformanceTracker.SafeNode safeNode =
                     tracker.track("InfoCodeConfigQueryContext#fillAggregates")) {
            InfoCodeInitByObjectParam initByObjectParam = toEquipmentInfoCodeInitByObjectParam();
            safeNode.recordStep("转换聚合根初始化参数");
            List<InfoCodeAggregate> aggregates = infoCodeGateway.of(initByObjectParam);
            safeNode.recordStep("创建聚合根");
            infoCodeGateway.fillInfoCodeDefineModel(aggregates);
            safeNode.recordStep("填充信息点定义模型");
            infoCodeGateway.fillInfoCodeDataModel(aggregates, null);
            safeNode.recordStep("填充信息点数据模型");
            if (CollectionUtils.isNotEmpty(aggregates)) {
                this.aggregates = aggregates;
            } else {
                this.failed = true;
            }
        }
    }

    /**
     * 转换对象排序器
     *
     * @param objects 对象列表
     * @return 排序器
     * @since 7.250601.0
     */
    @SuppressWarnings("unchecked")
    private Comparator<DtpRwdObjectDTO> toObjectComparator(@NonNull List<DtpRwdObjectDTO> objects) {
        List<DtpRwdObjectOrderParam> orders = this.param.getOrders();
        if (CollectionUtils.isEmpty(objects) || CollectionUtils.isEmpty(orders)) {
            return null;
        }
        // 查询建筑排序
        boolean orderByBuilding = orders.stream().anyMatch(order ->
                DtpRwdObjectOrderParam.COLUMN_BUILDING.equals(order.getColumn()));
        Map<String, Integer> buildingIdAndSequenceId = orderByBuilding
                ? queryBuildingIdAndSequenceId(objects)
                : Collections.emptyMap();
        // 查询楼层排序
        boolean orderByFloor = orders.stream().anyMatch(order ->
                DtpRwdObjectOrderParam.COLUMN_FLOOR.equals(order.getColumn()));
        Map<String, Integer> floorIdAndSequenceId = orderByFloor
                ? queryFloorIdAndSequenceId(objects)
                : Collections.emptyMap();
        Comparator<DtpRwdObjectDTO> rootComparator = (o1, o2) -> 0;
        for (DtpRwdObjectOrderParam order : orders) {
            String column = order.getColumn();
            rootComparator = rootComparator.thenComparing(item -> {
                switch (column) {
                    case DtpRwdObjectOrderParam.COLUMN_BUILDING:
                        return buildingIdAndSequenceId.get(item.analyzeProperties(BaseConvert.INFO_CODE_BUILDING_ID));
                    case DtpRwdObjectOrderParam.COLUMN_FLOOR:
                        return floorIdAndSequenceId.get(item.analyzeProperties(BaseConvert.INFO_CODE_FLOOR_ID));
                    default:
                        try {
                            PropertyDescriptor descriptor = item.cachePropertyDescriptor(column);
                            Object value = descriptor.getReadMethod().invoke(item);
                            if (ObjectUtils.isNotEmpty(value)) {
                                return value;
                            }
                            return item.analyzeProperties(column, Object.class);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            logProjectInstance().error("解析排序字段异常", e, Pair.of("column", column));
                            throw new ApplicationException(RequestServerErrorEnum.SERVICE_ERROR,
                                    String.format(LocaleMessageUtil.getMessage("key_49a2a9355d364befbfee05a91fe867c7"), column));
                        }
                }
            }, toFieldComparator(order));
        }
        return rootComparator;
    }

    /**
     * 查询楼层ID和楼层排序
     *
     * @param objects 对象列表
     * @return 楼层ID -> 楼层排序
     * @since 7.250601.0
     */
    private Map<String, Integer> queryFloorIdAndSequenceId(@NonNull List<DtpRwdObjectDTO> objects) {
        Set<String> objectIds = new HashSet<>();
        Set<String> floorIds = new HashSet<>();
        for (DtpRwdObjectDTO object : objects) {
            objectIds.add(object.getId());
            String floorId = object.analyzeProperties(BaseConvert.INFO_CODE_FLOOR_ID);
            if (StringUtils.isNotBlank(floorId)) {
                floorIds.add(floorId);
            }
        }
        if (CollectionUtils.isEmpty(floorIds)) {
            return Maps.newLinkedHashMap();
        }
        List<DtpRwdFloorDTO> floors = dtpRwdClientService.listFloorInByEquipmentId(objectIds,
                new DtpRwdObjectQueryParam().setId(floorIds).addWithColumn(
                        DtpRwdObjectDTO.Fields.id, DtpRwdFloorDTO.Fields.floorSequenceId));
        if (CollectionUtils.isEmpty(floors)) {
            return Maps.newLinkedHashMap();
        }
        return floors.stream().collect(Collectors.toMap(DtpRwdFloorDTO::getId,
                DtpRwdFloorDTO::getFloorSequenceId, (o1, o2) -> o1));
    }

    /**
     * 查询建筑ID和建筑排序
     *
     * @param objects 对象列表
     * @return 建筑ID -> 建筑顺序
     * @since 7.250601.0
     */
    private Map<String, Integer> queryBuildingIdAndSequenceId(@NonNull List<DtpRwdObjectDTO> objects) {
        Set<String> buildingIds = objects.stream()
                .map(object -> object.analyzeProperties(BaseConvert.INFO_CODE_BUILDING_ID))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        for (DtpRwdObjectDTO object : objects) {
            String buildingId = object.analyzeProperties(BaseConvert.INFO_CODE_BUILDING_ID);
            if (StringUtils.isNotBlank(buildingId)) {
                buildingIds.add(buildingId);
            }
        }
        if (CollectionUtils.isEmpty(buildingIds)) {
            return Maps.newLinkedHashMap();
        }
        List<DtpRwdBuildingDTO> buildings = dtpRwdClientService.listBuilding(new DtpRwdObjectQueryParam()
                .setId(buildingIds).addWithColumn(DtpRwdObjectDTO.Fields.id));
        if (CollectionUtils.isEmpty(buildings)) {
            return Maps.newLinkedHashMap();
        }
        Map<String, Integer> buildingIdAndSequenceId = new LinkedHashMap<>();
        for (int i = 0; i < buildings.size(); i++) {
            buildingIdAndSequenceId.put(buildings.get(i).getId(), i);
        }
        return buildingIdAndSequenceId;
    }

    /**
     * 转换字段比较器
     *
     * @param param 排序参数
     * @return 比较器
     * @since 7.2550601.0
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private Comparator toFieldComparator(DtpRwdObjectOrderParam param) {
        Comparator comparator = BooleanUtils.isTrue(param.getChineseOrder())
                ? Collator.getInstance(Locale.CHINA) : Comparator.naturalOrder();
        if (BooleanUtils.isFalse(param.getAsc())) {
            comparator = comparator.reversed();
        }
        return Comparator.nullsLast(comparator);
    }

    @Override
    public String logTag() {
        return "信息点配置-配置查询上下文";
    }
}
