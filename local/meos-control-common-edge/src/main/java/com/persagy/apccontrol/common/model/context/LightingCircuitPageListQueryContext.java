package com.persagy.apccontrol.common.model.context;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import cn.hutool.core.date.StopWatch;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.handler.LocalRedisCacheHandler;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataModel;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataSourceDefineModel;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandConfigLightingResultDataDTO;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDataSourceDTO;
import com.persagy.apccontrol.basic.model.dto.lighting.LightingSceneDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.*;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.lighting.LightingRunningDeviationTypeEnum;
import com.persagy.apccontrol.basic.model.page.DefaultListPage;
import com.persagy.apccontrol.basic.model.page.Pageable;
import com.persagy.apccontrol.basic.model.param.lighting.LightingControllerWithCircuitQueryParam;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.model.vo.lighting.LightingCircuitTagVO;
import com.persagy.apccontrol.basic.outside.dto.lighting.*;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.service.demand.lighting.DemandLightingQueryService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingCircuitSettingModeEnum;
import com.persagy.apccontrol.basic.structure.demand.strategy.lighting.DemandLightingStrategy;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ComparatorUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.bag.HashBag;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 照明-回路列表查询上下文
 *
 * @author JiYinchuan
 * @since 7.250101.0
 */
@Data
@Setter(AccessLevel.NONE)
@SuppressWarnings("unchecked")
public class LightingCircuitPageListQueryContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(LightingCircuitPageListQueryContext.class);
    private static final String LOG_TAG = "照明回路列表查询上下文";

    /**
     * 待查询信息点列表
     */
    private final List<String> infoCodes = Lists.newArrayList(
            InfoCodeEnum.MANUAL_AUTO_STATUS.getCode(),
            InfoCodeEnum.RUN_STATUS.getCode(),
            InfoCodeEnum.ORDER_FAIL_ALARM.getCode(),
            InfoCodeEnum.ONLINE_STATUS.getCode(),
            InfoCodeEnum.EQUIP_SWITCH_SET.getCode(),
            InfoCodeEnum.LOCAL_REMOTZE_STATUS.getCode(),
            InfoCodeEnum.I.getCode(),
            InfoCodeEnum.CONTROL_DEVIATION_STATUS.getCode()
    );

    // ------------------------- 入参 -------------------------

    /**
     * 查询参数
     */
    private final LightingControllerWithCircuitQueryParam param;

    /**
     * 物理世界服务
     */
    private final DtpRwdClientService dtpRwdClientService = SpringUtil.getBean(DtpRwdClientService.class);

    /**
     * 照明需求策略
     */
    private final DemandLightingStrategy demandLightingStrategy = SpringUtil.getBean(DemandLightingStrategy.class);

    /**
     * 照明需求查询服务
     */
    private final DemandLightingQueryService demandLightingQueryService = SpringUtil.getBean(DemandLightingQueryService.class);

    /**
     * Redis缓存处理器
     */
    private final LocalRedisCacheHandler localRedisCacheHandler = SpringUtil.getBean(LocalRedisCacheHandler.class);

    /**
     * IOT数据服务
     */
    private final AbstractIotDataCacheServer<FeedbackInfoMsgMqttVO> abstractIotDataCacheServer = SpringUtil.getBean(AbstractIotDataCacheServer.class);

    /**
     * 公共服务
     */
    private final AbstractCommonServer abstractCommonServer = SpringUtil.getBean(AbstractCommonServer.class);

    /**
     * 计时器
     */
    private final StopWatch stopWatch = StopWatch.create("查询照明回路列表");

    // ------------------------- 过程 -------------------------

    /**
     * 照明回路列表
     */
    private List<DtpRwdCircuitDTO> circuits = Collections.emptyList();

    /**
     * 分页对象
     */
    private DefaultListPage<List<DtpRwdCircuitDTO>> pageable = new DefaultListPage<>(
            Pageable.DEFAULT_PAGE_NO, Pageable.DEFAULT_PAGE_SIZE);

    /**
     * 照明回路ID -> [信息点编码 -> 信息点值模型]
     */
    private Map<String, Map<String, InfoCodeDataModel>> circuitIdAndInfoCodeData = Collections.emptyMap();

    /**
     * 信息点 -> 信息点配置
     */
    private Map<String, RcInfoCodeCfg> infoCodeAndConfig = Collections.emptyMap();

    /**
     * 照明回路ID -> 存在规则的IOT信息点
     */
    private Map<String, Set<String>> circuitIdAndExistsRuleInfoCodes = Collections.emptyMap();

    /**
     * 管控照明回路ID -> 管控信息点
     */
    private Map<String, Set<String>> controlCircuitIdAndInfoCodes = Collections.emptyMap();

    /**
     * 照明回路ID -> 关联控制模块
     */
    private Map<String, DtpRwdObjectDTO> circuitIdAndController = Collections.emptyMap();

    /**
     * 照明回路ID -> 关联低压末端配电柜
     */
    private Map<String, DtpRwdObjectDTO> circuitIdAndDistributionCabinet = Collections.emptyMap();

    /**
     * 照明回路ID -> 关联异常事件
     */
    private Map<String, List<ExceptionEventBaseInfoVO>> circuitIdAndExceptionEvents = Collections.emptyMap();

    /**
     * 照明回路ID -> 服务空间
     */
    private Map<String, List<DtpRwdSpaceDTO>> circuitIdAndServiceSpaces = Collections.emptyMap();

    /**
     * 楼层ID -> 所在楼层
     */
    private Map<String, DtpRwdFloorDTO> floorIdAndData = Collections.emptyMap();

    /**
     * 建筑ID -> 所在建筑
     */
    private Map<String, DtpRwdBuildingDTO> buildingIdAndData = Collections.emptyMap();

    /**
     * 当前场景
     */
    @Nullable
    private LightingSceneDTO scene = null;

    /**
     * 照明回路ID -> 配置结果
     */
    private Map<String, DemandConfigLightingResultDataDTO> circuitIdAndConfigResult = Collections.emptyMap();

    /**
     * 照明回路ID -> 照明回路标签
     */
    private Map<String, LightingCircuitTagVO> circuitIdAndTag = Collections.emptyMap();

    // ------------------------- 结果 -------------------------

    /**
     * 是否失败
     */
    private boolean failed = false;

    // ------------------------- 行为 -------------------------

    /**
     * 初始化照明回路
     *
     * @since 7.250101.0
     */
    public void initCircuits() {
        this.stopWatch.start("初始化照明回路");
        String projectId = param.getProjectId();
        DemandTemplateTypeEnum templateType = param.getTemplateType();
        DemandInstanceTypeEnum instanceType = param.getInstanceType();
        String instanceId = param.getInstanceId();
        String keyword = param.getKeyword();
        List<String> funcTypes = param.getFuncType();
        List<String> lampFuncTypes = param.getLampFuncType();

        List<DtpRwdCircuitDTO> circuits = ObjectUtils.allNotNull(instanceType, instanceId)
                ? demandLightingStrategy.listCircuitsByInstance(projectId, templateType, instanceType, instanceId)
                : demandLightingStrategy.listCircuits(projectId, templateType, null);
        if (CollectionUtils.isNotEmpty(circuits)) {
            circuits.removeIf(circuit -> {
                // 存在关键字时, 过滤localId和localName
                boolean keywordNonMatch = StringUtils.isNotBlank(keyword) &&
                        !StringUtils.contains(circuit.getLocalId(), keyword)
                        && !StringUtils.contains(circuit.getLocalName(), keyword);
                // 存在功能类型时过滤
                boolean funcTypeNonMatch = CollectionUtils.isNotEmpty(funcTypes)
                        && funcTypes.stream().noneMatch(item -> StringUtils.equals(item, circuit.getFuncType()));
                // 存在灯具功能类型时过滤
                boolean lampFuncTypeNonMatch = CollectionUtils.isNotEmpty(lampFuncTypes)
                        && lampFuncTypes.stream().noneMatch(item -> StringUtils.equals(item, circuit.getLampFuncType()));
                // 任何一个未匹配则删除
                return keywordNonMatch | funcTypeNonMatch | lampFuncTypeNonMatch;
            });
        }
        if (CollectionUtils.isEmpty(circuits)) {
            this.failed = true;
            this.stopWatch.stop();
            return;
        }
        this.circuits = circuits;
        this.pageable = new DefaultListPage<>(param.getPage(), param.getPageSize(), circuits.size(), circuits);
        this.stopWatch.stop();
    }

    /**
     * 处理IOT信息点
     *
     * @since 7.250101.0
     */
    public void handleIotInfoCode() {
        if (CollectionUtils.isEmpty(this.circuits)) {
            return;
        }
        this.stopWatch.start("处理IOT信息点");
        String projectId = param.getProjectId();
        Set<String> circuitIds = this.circuits.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());

        List<FeedbackInfoMsgMqttVO> feedbacks = abstractIotDataCacheServer.queryIotRealDataListByList(
                ContextUtil.getGroupCode(), projectId, circuitIds, this.infoCodes);
        Map<String, Map<String, InfoCodeDataModel>> circuitIdAndInfoCodeData = new HashMap<>();
        // // 信息点 -> 信息点配置
        Map<String, RcInfoCodeCfg> infoCodeAndConfig = localRedisCacheHandler
                .getInfoCodeConfigMap(DtpRwdClassCodeEnum.LIGHTING_CIRCUIT.value());
        if (CollectionUtils.isNotEmpty(feedbacks)) {
            for (FeedbackInfoMsgMqttVO feedback : feedbacks) {
                String objectId = feedback.getObjectId();
                String infoCode = feedback.getInfoCode();
                String dataValue = feedback.getDataValue();
                LocalDateTime dataTime = feedback.toDataTime();

                // 处理信息点值
                MapExecutor.dynamicPut(circuitIdAndInfoCodeData, objectId, infoCode, new InfoCodeDataModel(
                        infoCode, dataValue, dataTime, null), HashMap::new);
            }
        }
        // 过滤照明回路动态IOT信息点
        this.circuits = filterCircuitByInfoCode(this.circuits, circuitIdAndInfoCodeData);
        if (CollectionUtils.isEmpty(this.circuits)) {
            this.failed = true;
            this.stopWatch.stop();
            return;
        }
        this.pageable.setRecords(circuits).setTotalCount(circuits.size());
        this.circuitIdAndInfoCodeData = circuitIdAndInfoCodeData;
        this.infoCodeAndConfig = infoCodeAndConfig;
        this.stopWatch.stop();
    }

    /**
     * 处理照明回路ID和配置结果映射
     *
     * @param circuitIdAndConfigResultFunction 照明回路ID -> 配置结果
     * @since 7.250101.0
     */
    public void handleCircuitIdAndConfigResult(
            @NonNull Function<Set<String>, Map<String, DemandConfigLightingResultDataDTO>> circuitIdAndConfigResultFunction) {
        if (CollectionUtils.isEmpty(this.circuits)) {
            return;
        }
        this.stopWatch.start("查询照明回路关联配置结果");
        String runningDemandType = param.getRunningDemandType();
        Set<String> circuitIds = this.circuits.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        Map<String, DemandConfigLightingResultDataDTO> circuitIdAndConfigResult =
                circuitIdAndConfigResultFunction.apply(circuitIds);
        // 过滤照明回路需求类型
        if (StringUtils.isNotBlank(runningDemandType)) {
            if (MapUtils.isNotEmpty(circuitIdAndConfigResult)) {
                this.circuits.removeIf(item -> {
                    DemandConfigLightingResultDataDTO configResult = circuitIdAndConfigResult.get(item.getId());
                    DemandLightingCircuitSettingModeEnum settingMode = parseCircuitSettingMode(configResult);
                    return !StringUtils.equals(settingMode.value(), runningDemandType);
                });
            } else {
                this.circuits = Collections.emptyList();
            }
            if (CollectionUtils.isEmpty(this.circuits)) {
                this.failed = true;
                this.stopWatch.stop();
                return;
            }
        }
        if (MapUtils.isNotEmpty(circuitIdAndConfigResult)) {
            this.circuitIdAndConfigResult = circuitIdAndConfigResult;
        }
        this.pageable.setRecords(circuits).setTotalCount(circuits.size());
        this.stopWatch.stop();
    }

    /**
     * 处理照明回路ID和标签
     *
     * @param circuitIdAndTagFunction 照明回路ID -> 标签
     * @since 7.250101.0
     */
    public void handleCircuitIdAndTag(
            @NonNull Function<Set<String>, Map<String, LightingCircuitTagVO>> circuitIdAndTagFunction) {
        if (CollectionUtils.isEmpty(this.circuits)) {
            return;
        }
        this.stopWatch.start("查询照明回路关联标签");
        List<String> deviationTypes = param.getRunningDeviationType();
        Set<String> circuitIds = this.circuits.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        Map<String, LightingCircuitTagVO> circuitIdAndTag = circuitIdAndTagFunction.apply(circuitIds);
        // 过滤照明回路运行偏差类型
        if (CollectionUtils.isNotEmpty(deviationTypes)) {
            if (MapUtils.isNotEmpty(circuitIdAndTag)) {
                this.circuits.removeIf(item -> {
                    LightingCircuitTagVO circuitTag = circuitIdAndTag.get(item.getId());
                    if (Objects.isNull(circuitTag)) {
                        return true;
                    }
                    LightingRunningDeviationTypeEnum deviationType = circuitTag.getRunningDeviationType();
                    if (Objects.isNull(deviationType)) {
                        return true;
                    }
                    return !deviationTypes.contains(deviationType.value());
                });
            } else {
                this.circuits = Collections.emptyList();
            }
            if (CollectionUtils.isEmpty(this.circuits)) {
                this.failed = true;
                this.stopWatch.stop();
                return;
            }
        }
        if (MapUtils.isNotEmpty(circuitIdAndTag)) {
            this.circuitIdAndTag = circuitIdAndTag;
        }
        this.pageable.setRecords(circuits).setTotalCount(circuits.size());
        this.stopWatch.stop();
    }

    /**
     * 处理照明回路服务空间
     *
     * @since 7.250101.0
     */
    public void handleCircuitServiceSpaces() {
        if (CollectionUtils.isEmpty(this.circuits)) {
            return;
        }
        this.stopWatch.start("填充照明回路服务空间");
        String projectId = param.getProjectId();
        DemandTemplateTypeEnum templateType = param.getTemplateType();
        Set<String> serviceRoomFuncTypes = param.getServiceRoomFuncTypes();
        Set<String> circuitIds = this.circuits.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        List<DtpRwdSpaceDTO> spaces = demandLightingStrategy.listSpacesByCircuit(projectId, templateType, circuitIds, null);
        // 过滤照明回路服务空间功能类型
        if (CollectionUtils.isNotEmpty(serviceRoomFuncTypes)) {
            if (CollectionUtils.isNotEmpty(spaces)) {
                for (DtpRwdSpaceDTO space : spaces) {
                    String roomFuncType = space.getRoomFuncType();
                    Set<String> relObjIds = space.getRelObjIds();
                    if (CollectionUtils.isNotEmpty(relObjIds) && !serviceRoomFuncTypes.contains(roomFuncType)) {
                        this.circuits.removeIf(item -> relObjIds.contains(item.getId()));
                    }
                }
            } else {
                this.circuits = Collections.emptyList();
            }
            if (CollectionUtils.isEmpty(this.circuits)) {
                this.failed = true;
                this.stopWatch.stop();
                return;
            }
        }
        if (CollectionUtils.isNotEmpty(spaces)) {
            Map<String, List<DtpRwdSpaceDTO>> circuitIdAndServiceSpaces = new HashMap<>();
            for (DtpRwdSpaceDTO space : spaces) {
                Set<String> relObjIds = space.getRelObjIds();
                if (CollectionUtils.isNotEmpty(relObjIds)) {
                    for (String circuitId : relObjIds) {
                        MapExecutor.dynamicPut(circuitIdAndServiceSpaces, circuitId, space, ArrayList::new);
                    }
                }
            }
            this.circuitIdAndServiceSpaces = circuitIdAndServiceSpaces;
        }
        this.stopWatch.stop();
    }

    /**
     * 填充关联建筑
     *
     * @since 7.250101.0
     */
    @SuppressWarnings("DuplicatedCode")
    public void fillRelBuilding() {
        Set<String> buildingIds = this.circuits.stream()
                .map(DtpRwdCircuitDTO::getBuildingId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        if (MapUtils.isNotEmpty(this.circuitIdAndServiceSpaces)) {
            buildingIds.addAll(this.circuitIdAndServiceSpaces.values().stream()
                    .flatMap(Collection::stream)
                    .map(DtpRwdSpaceDTO::getBuildingId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet()));
        }
        if (CollectionUtils.isEmpty(buildingIds)) {
            return;
        }
        Map<String, DtpRwdBuildingDTO> buildingIdAndData = dtpRwdClientService.listBuilding(
                        new DtpRwdObjectQueryParam().setId(buildingIds).addWithColumn(DtpRwdObjectDTO.Fields.id,
                                DtpRwdObjectDTO.Fields.localName, DtpRwdBuildingDTO.Fields.buildingSequenceId)).stream()
                .collect(Collectors.toMap(DtpRwdObjectDTO::getId, Function.identity(), (o1, o2) -> o1));
        for (DtpRwdCircuitDTO circuit : this.circuits) {
            String buildingId = circuit.getBuildingId();
            DtpRwdBuildingDTO inBuilding = buildingIdAndData.get(buildingId);
            if (Objects.nonNull(inBuilding)) {
                circuit.setBuildingSequenceId(inBuilding.getBuildingSequenceId());
            }
        }
        this.buildingIdAndData = buildingIdAndData;
    }

    /**
     * 填充关联楼层
     *
     * @since 7.250101.0
     */
    @SuppressWarnings("DuplicatedCode")
    public void fillRelFloor() {
        Set<String> floorIds = this.circuits.stream()
                .map(DtpRwdCircuitDTO::getFloorId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        if (MapUtils.isNotEmpty(this.circuitIdAndServiceSpaces)) {
            floorIds.addAll(this.circuitIdAndServiceSpaces.values().stream()
                    .flatMap(Collection::stream)
                    .map(DtpRwdSpaceDTO::getFloorId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet()));
        }
        if (CollectionUtils.isEmpty(floorIds)) {
            return;
        }
        Map<String, DtpRwdFloorDTO> floorIdAndData = dtpRwdClientService.listFloor(new DtpRwdObjectQueryParam()
                        .setId(floorIds).addWithColumn(DtpRwdObjectDTO.Fields.id,
                                DtpRwdObjectDTO.Fields.localName,
                                DtpRwdFloorDTO.Fields.floorSequenceId)).stream()
                .collect(Collectors.toMap(DtpRwdObjectDTO::getId, Function.identity(), (o1, o2) -> o1));
        for (DtpRwdCircuitDTO circuit : this.circuits) {
            String floorId = circuit.getFloorId();
            DtpRwdFloorDTO inFloor = floorIdAndData.get(floorId);
            if (Objects.nonNull(inFloor)) {
                circuit.setFloorSequenceId(inFloor.getFloorSequenceId());
            }
        }
        this.floorIdAndData = floorIdAndData;
    }

    /**
     * 处理照明回路分页
     *
     * @since 7.250101.0
     */
    public void handleCircuitPage() {
        if (CollectionUtils.isEmpty(this.circuits)) {
            return;
        }
        // 对照明回路根据建筑/楼层/名称排序
        this.circuits.sort(ComparatorUtils.chainedComparator(
                Comparator.nullsLast(Comparator.comparingInt(item -> Optional.ofNullable(item.getBuildingSequenceId()).orElse(999))),
                Comparator.nullsLast(Comparator.comparingInt(item -> Optional.ofNullable(item.getFloorSequenceId()).orElse(999))),
                Comparator.nullsLast(Comparator.comparing(item -> Optional.ofNullable(item.getLocalName()).orElse("")))
        ));
        Integer page = param.getPage();
        Integer pageSize = param.getPageSize();
        if (ObjectUtils.allNotNull(page, pageSize)) {
            List<List<DtpRwdCircuitDTO>> partition = Lists.partition(this.circuits, pageSize);
            // 查询页数超过页码时, 返回空
            if (page > partition.size()) {
                this.circuits = Collections.emptyList();
                this.failed = true;
                return;
            }
            this.circuits = partition.get(page - 1);
        }
    }

    /**
     * 填充照明回路IOT信息点规则
     *
     * @since 7.250101.0
     */
    public void fillCircuitIotInfoCodeExistsRule() {
        if (CollectionUtils.isEmpty(this.circuits)) {
            return;
        }
//        this.stopWatch.start("填充照明回路IOT信息点规则");
        Map<String, Set<String>> classCodeAndIds = this.circuits.stream().collect(Collectors.groupingBy(
                item -> DtpRwdClassCodeEnum.LIGHTING_CIRCUIT.value(),
                Collectors.mapping(DtpRwdObjectDTO::getId, Collectors.toSet())));
        Map<String, Set<String>> circuitIdAndExistsRuleInfoCodes = abstractIotDataCacheServer
                .mapObjectIdAndExistsRuleInfoCodes(classCodeAndIds, this.infoCodes);
//        this.stopWatch.stop();
        if (MapUtils.isEmpty(circuitIdAndExistsRuleInfoCodes)) {
            return;
        }
        this.circuitIdAndExistsRuleInfoCodes = circuitIdAndExistsRuleInfoCodes;
    }

    /**
     * 填充管控照明回路
     *
     * @since 7.250101.0
     */
    public void fillControlCircuits() {
        if (CollectionUtils.isEmpty(this.circuits)) {
            return;
        }
//        this.stopWatch.start("填充管控照明回路");
        List<DtpRwdTwinControlObjectDTO> controlCircuits = dtpRwdClientService.listTwinControlObjects(false,
                param.getProjectId(), Lists.newArrayList(DtpRwdClassCodeEnum.LIGHTING_CIRCUIT.value()));
//        this.stopWatch.stop();
        if (CollectionUtils.isEmpty(controlCircuits)) {
            return;
        }
        Map<String, Set<String>> controlCircuitIdAndInfoCodes = new HashMap<>();
        for (DtpRwdTwinControlObjectDTO controlCircuit : controlCircuits) {
            String circuitId = controlCircuit.getObjectId();
            List<DtpRwdTwinControlInfoCodeDTO> infoCodes = controlCircuit.getInfoCodes();
            if (CollectionUtils.isNotEmpty(infoCodes)) {
                infoCodes.forEach(item -> MapExecutor.dynamicPut(controlCircuitIdAndInfoCodes,
                        circuitId, item.getCode(), HashSet::new));
            }
        }
        if (MapUtils.isEmpty(controlCircuitIdAndInfoCodes)) {
            return;
        }
        this.controlCircuitIdAndInfoCodes = controlCircuitIdAndInfoCodes;
    }

    /**
     * 填充照明回路关联控制模块
     *
     * @since 7.250101.0
     */
    public void fillCircuitRelController() {
        if (CollectionUtils.isEmpty(this.circuits)) {
            return;
        }
//        this.stopWatch.start("填充照明回路关联控制模块");
        String projectId = param.getProjectId();
        DemandTemplateTypeEnum templateType = param.getTemplateType();
        Set<String> circuitIds = this.circuits.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        this.circuitIdAndController = demandLightingStrategy.mapCircuitIdAndController(
                projectId, templateType, circuitIds, null);
//        this.stopWatch.stop();
    }

    /**
     * 填充照明回路关联低压末端配电柜
     *
     * @since 7.250101.0
     */
    public void fillCircuitIdAndDistributionCabinet() {
        if (CollectionUtils.isEmpty(this.circuits)) {
            return;
        }
//        this.stopWatch.start("填充照明回路关联低压末端配电柜");
        String projectId = param.getProjectId();
        Set<String> circuitIds = this.circuits.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        this.circuitIdAndDistributionCabinet = demandLightingStrategy
                .mapCircuitIdAndDistributionCabinet(projectId, circuitIds);
//        this.stopWatch.stop();
    }

    /**
     * 填充照明回路关联异常事件
     *
     * @since 7.250101.0
     */
    public void fillCircuitIdAndExceptionEvents() {
        if (CollectionUtils.isEmpty(this.circuits)) {
            return;
        }
//        this.stopWatch.start("填充照明回路关联异常事件");
        Set<String> circuitIds = this.circuits.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        this.circuitIdAndExceptionEvents = abstractCommonServer.mapRiskEvent(circuitIds, null);
//        this.stopWatch.stop();
    }

    /**
     * 填充当前场景
     *
     * @since 7.250101.0
     */
    public void fillScene() {
//        this.stopWatch.start("填充当前场景");
        String projectId = param.getProjectId();
        DemandTemplateTypeEnum templateType = param.getTemplateType();
        this.scene = demandLightingStrategy.getSceneByNow(projectId, templateType);
//        this.stopWatch.stop();
    }

    /**
     * 转换结果
     *
     * @return 结果
     * @since 7.250101.0
     */
    public LightingDataDto toLightingDataDto() {
        if (CollectionUtils.isEmpty(this.circuits)) {
            return LightingDataDto.empty();
        }
        // 获取所有对象均存在的信息点编码
        Set<String> existsRuleInfoCodes = listBothExistsRuleInfoCodes();
        List<LightingRecords> records = new ArrayList<>();
        for (DtpRwdCircuitDTO circuit : this.circuits) {
            String circuitId = circuit.getId();

            // 构建构建基础信息
            BasicInfos basicInfo = buildBasicInfos(circuit);
            // 构建运行参数
            List<InfoCodeParameters> runningParameters = buildRunningParameters(circuitId, existsRuleInfoCodes);
            // 构建操作列
            OperationColumn operationColumn = buildOperationColumn(circuitId);
            // 构建限制信息点
            Map<String, Object> restrictInfos = buildRestrictInfos(circuitId);
            // 异常事件
            List<ExceptionEventBaseInfoVO> exceptions = this.circuitIdAndExceptionEvents
                    .getOrDefault(circuitId, Collections.emptyList());
            LightingRecords record = new LightingRecords();
            record.setId(circuitId);
            record.setName(circuit.getName());
            record.setLocalId(circuit.getLocalId());
            record.setLocalName(circuit.getLocalName());
            record.setObjType(circuit.getObjType());
            record.setClassCode(circuit.getClassCode());
            record.setValid(circuit.getValid());
            record.setBuildingSequenceId(circuit.getBuildingSequenceId());
            record.setFloorSequenceId(circuit.getFloorSequenceId());
            record.setBasicInfos(basicInfo);
            record.setRunningParameters(runningParameters);
            record.setAbnormalEventCount(exceptions.size());
            record.setAbnormalEvents(exceptions);
            record.setOperationColumn(operationColumn);
            record.setRestrictInfos(restrictInfos);
            records.add(record);
        }
        LightingDataDto result = new LightingDataDto();
        result.setTotalCount(this.pageable.getTotalCount());
        result.setTotalPage(this.pageable.getTotalPage());
        result.setRecords(records);
        return result;
    }

    /**
     * 打印执行时间
     *
     * @since 7.250101.0
     */
    public void printExecuteTime() {
        LogExecutor.project().debug(LOGGER, LOG_TAG, "查询照明回路列表",
                Pair.of("executeTime", this.stopWatch.prettyPrint(TimeUnit.MILLISECONDS)));
    }

    /**
     * 转换照明回路ID和信息点值映射
     *
     * @return 照明回路ID -> 信息点值
     * @since 7.250402.0
     */
    public Map<String, Map<String, Object>> toCircuitIdAndInfoCodeValue() {
        if (MapUtils.isEmpty(this.circuitIdAndInfoCodeData)) {
            return Maps.newHashMap();
        }
        Map<String, Map<String, Object>> result = new HashMap<>();
        for (Map.Entry<String, Map<String, InfoCodeDataModel>> entry : this.circuitIdAndInfoCodeData.entrySet()) {
            String circuitId = entry.getKey();
            Map<String, InfoCodeDataModel> infoCodeAndData = entry.getValue();
            if (MapUtils.isNotEmpty(infoCodeAndData)) {
                for (Map.Entry<String, InfoCodeDataModel> innerEntry : infoCodeAndData.entrySet()) {
                    String infoCode = innerEntry.getKey();
                    InfoCodeDataModel dataModel = innerEntry.getValue();
                    if (Objects.nonNull(dataModel)) {
                        // noinspection DataFlowIssue
                        MapExecutor.dynamicPut(result, circuitId, infoCode, dataModel.getDataValue(), HashMap::new);
                    }
                }
            }
        }
        return result;
    }

    /**
     * 获取所有对象均存在的信息点编码
     *
     * @since 7.250101.0
     */
    private Set<String> listBothExistsRuleInfoCodes() {
        if (CollectionUtils.isEmpty(this.circuits)) {
            return Sets.newHashSet();
        }
        // 信息点包, 用于记录存在规则的信息点出现次数
        Bag<String> infoCodeBag = new HashBag<>();
        for (DtpRwdCircuitDTO circuit : this.circuits) {
            String circuitId = circuit.getId();

            Set<String> existsRules = this.circuitIdAndExistsRuleInfoCodes.get(circuitId);
            if (CollectionUtils.isEmpty(existsRules)) {
                continue;
            }
            this.infoCodes.stream().filter(existsRules::contains).forEach(infoCodeBag::add);
        }
        return this.infoCodes.stream()
                .filter(item -> infoCodeBag.getCount(item) > 0)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * 构建限制信息点
     *
     * @param circuitId 照明回路ID
     * @return 限制信息点
     * @since 7.250101.0
     */
    private Map<String, Object> buildRestrictInfos(@NonNull String circuitId) {
        Map<String, InfoCodeDataModel> infoCodeAndData = this.circuitIdAndInfoCodeData
                .getOrDefault(circuitId, Collections.emptyMap());
        Set<String> existsRuleInfoCodes = this.circuitIdAndExistsRuleInfoCodes
                .getOrDefault(circuitId, Collections.emptySet());

        // 默认值为[null], 存在规则有值时为对应的值, 无值时为[-1]
        Object localRemoteStatus = null;
        Object onlineStatus = null;
        if (existsRuleInfoCodes.contains(InfoCodeEnum.LOCAL_REMOTZE_STATUS.value())) {
            InfoCodeDataModel dataModel = infoCodeAndData.get(InfoCodeEnum.LOCAL_REMOTZE_STATUS.value());
            localRemoteStatus = Objects.nonNull(dataModel) && Objects.nonNull(dataModel.getDataValue())
                    ? dataModel.getDataValue() : "-1";
        }
        if (existsRuleInfoCodes.contains(InfoCodeEnum.ONLINE_STATUS.value())) {
            InfoCodeDataModel dataModel = infoCodeAndData.get(InfoCodeEnum.ONLINE_STATUS.value());
            onlineStatus = Objects.nonNull(dataModel) && Objects.nonNull(dataModel.getDataValue())
                    ? dataModel.getDataValue() : "-1";
        }
        Map<String, Object> result = new HashMap<>();
        result.put(InfoCodeEnum.LOCAL_REMOTZE_STATUS.value(), localRemoteStatus);
        result.put(InfoCodeEnum.ONLINE_STATUS.value(), onlineStatus);
        return result;
    }

    /**
     * 构建操作列
     *
     * @param circuitId 照明回路ID
     * @return 操作列
     * @since 7.250101.0
     */
    private OperationColumn buildOperationColumn(@NonNull String circuitId) {
        Map<String, InfoCodeDataModel> infoCodeAndData = Optional.ofNullable(
                this.circuitIdAndInfoCodeData.get(circuitId)).orElse(Collections.emptyMap());
        Set<String> controlInfoCodes = this.controlCircuitIdAndInfoCodes
                .getOrDefault(circuitId, Collections.emptySet());

        // 手自动为管控信息点时, 添加运行模式
        RunningModeDTO runningMode = null;
        if (controlInfoCodes.contains(InfoCodeEnum.MANUAL_AUTO_STATUS.value())) {
            // 反馈值存在手自动状态时, 设置运行模式值
            InfoCodeDataModel manualAutoStatusDataModel = infoCodeAndData.get(InfoCodeEnum.MANUAL_AUTO_STATUS.value());
            runningMode = new RunningModeDTO();
            runningMode.setModel(Objects.nonNull(manualAutoStatusDataModel) ? manualAutoStatusDataModel.getDataValue() : null);
            runningMode.setOperateReason("");
        }
        // 开关状态为管控信息点时, 添加开关状态
        Object equipSwitchSet = null;
        if (controlInfoCodes.contains(InfoCodeEnum.EQUIP_SWITCH_SET.value())) {
            // 反馈值存在开关状态时, 添加开关状态只
            InfoCodeDataModel equipSwitchSetDataModel = infoCodeAndData.get(InfoCodeEnum.EQUIP_SWITCH_SET.value());
            equipSwitchSet = Objects.nonNull(equipSwitchSetDataModel) ? equipSwitchSetDataModel.getDataValue() : null;
        }
        OperationColumn operationColumn = new OperationColumn();
        operationColumn.setManualAutoSet(runningMode);
        operationColumn.setEquipSwitchSet(equipSwitchSet);
        return operationColumn;
    }

    /**
     * 构建运行参数
     *
     * @param circuitId 照明回路ID
     * @param infoCodes 信息点编码
     * @return 运行参数
     * @since 7.250101.0
     */
    private List<InfoCodeParameters> buildRunningParameters(@NonNull String circuitId, @NonNull Set<String> infoCodes) {
        Map<String, InfoCodeDataModel> infoCodeAndData = this.circuitIdAndInfoCodeData.get(circuitId);
        DemandConfigLightingResultDataDTO configResult = this.circuitIdAndConfigResult.get(circuitId);
        LightingCircuitTagVO circuitTag = this.circuitIdAndTag.get(circuitId);
        if (MapUtils.isEmpty(infoCodeAndData)) {
            return Lists.newArrayList();
        }
        List<InfoCodeParameters> result = new ArrayList<>();
        for (String infoCode : infoCodes) {
            // 控制偏差状态前端不渲染
            if (StringUtils.equals(infoCode, InfoCodeEnum.CONTROL_DEVIATION_STATUS.getCode())) {
                continue;
            }
            InfoCodeDataModel dataModel = infoCodeAndData.get(infoCode);
            RcInfoCodeCfg infoCodeConfig = this.infoCodeAndConfig.get(infoCode);

            String infoCodeName = Objects.nonNull(infoCodeConfig) ? infoCodeConfig.getName() : "";
            // 反馈值不存在时补全数据, 避免前端渲染异常
            if (Objects.isNull(dataModel)) {
                result.add(InfoCodeParameters.ofEmpty(infoCode, infoCodeName));
                continue;
            }
            Object dataValue = dataModel.getDataValue();
            LocalDateTime dataTime = dataModel.getDataTime();

            // 处理信息点定义
            InfoCodeDataSourceDefineModel infoCodeSourceDefine = null;
            JSONObject additionalValue = new JSONObject();
            // 优先从配置中解析信息点定义
            if (Objects.nonNull(infoCodeConfig)) {
                additionalValue.put("unit", infoCodeConfig.getUnit());
                List<DtpKnowledgeInfoCodeDataSourceDTO> configDataSources = infoCodeConfig.getDataSource();
                if (CollectionUtils.isNotEmpty(configDataSources)) {
                    for (DtpKnowledgeInfoCodeDataSourceDTO configDataSource : configDataSources) {
                        if (StringUtils.equals(configDataSource.getCode(), String.valueOf(dataValue))) {
                            infoCodeSourceDefine = new InfoCodeDataSourceDefineModel(
                                    configDataSource.getCode(), configDataSource.getName());
                            break;
                        }
                    }
                }
            }
            // 从配置中无法解析出信息点定义时写入默认信息点定义
            if (Objects.isNull(infoCodeSourceDefine)) {
                infoCodeSourceDefine = new InfoCodeDataSourceDefineModel(infoCodeName,
                        Objects.nonNull(dataValue) ? dataValue.toString() : LocaleMessageUtil.getMessage("key_08241d179eeb4c989389940b30544847"));
            }
            Object infoCodeValue = Objects.nonNull(dataValue) ? dataValue : null;
            String updateTime = Objects.nonNull(dataTime)
                    ? dataTime.format(DateTimeFormatter.ofPattern(DateUtils.SDFSECOND))
                    : null;
            // 添加信息点参数
            InfoCodeParameters parameter = new InfoCodeParameters();
            parameter.setInfoCode(infoCode);
            parameter.setInfoCodeName(infoCodeName);
            parameter.setDataSource(Lists.newArrayList(infoCodeSourceDefine));
            parameter.setInfoCodeValue(infoCodeValue);
            parameter.setUpdateTime(updateTime);
            parameter.setAdditionalMapValue(additionalValue);
            result.add(parameter);
        }
        // 添加运行需求类型参数
        result.add(generateRunningDemandTypeParameter(configResult, circuitTag));
        return result;
    }

    /**
     * 生成运行偏差类型参数
     *
     * @param configResult 配置结果
     * @param circuitTag   照明回路标签
     * @return 运行偏差类型参数
     * @since 7.250101.0
     */
    private InfoCodeParameters generateRunningDemandTypeParameter(
            @Nullable DemandConfigLightingResultDataDTO configResult,
            @Nullable LightingCircuitTagVO circuitTag) {
        DemandLightingCircuitSettingModeEnum settingMode = parseCircuitSettingMode(configResult);
        JSONObject additional = new JSONObject();
        if (Objects.nonNull(circuitTag) && Objects.nonNull(circuitTag.getRunningDeviationType())) {
            // 存在需求偏差时, 添加信息点名称为需求偏差名称
            additional.put("infoCodeName", circuitTag.getRunningDeviationType().getReasonPhrase());
        } else if (settingMode == DemandLightingCircuitSettingModeEnum.FEEDBACK_ADJUSTMENT
                && Objects.nonNull(configResult)) {
            // 不存在需求偏差且为反馈调节时, 添加反馈调节配置
            additional.put("feedbackAdjustmentConfig", configResult.getFeedbackAdjustSettings());
        }
        if (Objects.nonNull(this.scene)) {
            additional.put("relDemandTemplateId", this.scene.getRelDemandTemplateId());
        }
        InfoCodeParameters runningDemandTypeParameter = new InfoCodeParameters();
        runningDemandTypeParameter.setInfoCode("runningDemandType");
        runningDemandTypeParameter.setInfoCodeName(LocaleMessageUtil.getMessage("key_db9a4db76b27490892d99092f9a0fd45"));
        runningDemandTypeParameter.setInfoCodeValue(settingMode.getReasonPhrase());
        runningDemandTypeParameter.setAdditionalMapValue(additional);
        return runningDemandTypeParameter;
    }

    /**
     * 解析照明模式配置模式
     *
     * @param configResult 配置结果
     * @return 照明模式配置模式
     * @since 7.250101.0
     */
    private DemandLightingCircuitSettingModeEnum parseCircuitSettingMode(
            @Nullable DemandConfigLightingResultDataDTO configResult) {
        DemandLightingCircuitSettingModeEnum settingMode = DemandLightingCircuitSettingModeEnum.NOT_CONFIGURED;
        if (Objects.nonNull(configResult) && Objects.nonNull(configResult.getCircuitSettingMode())) {
            settingMode = configResult.getCircuitSettingMode();
        }
        return settingMode;
    }

    /**
     * 构建基础信息
     *
     * @param circuit 照明回路
     * @return 基础信息
     * @since 7.250101.0
     */
    private BasicInfos buildBasicInfos(@NonNull DtpRwdCircuitDTO circuit) {
        String circuitId = circuit.getId();
        String funcType = circuit.getFuncType();
        String lampFuncType = circuit.getLampFuncType();

        // 关联配电柜
        DtpRwdObjectDTO relDistributionCabinet = this.circuitIdAndDistributionCabinet.get(circuitId);
        // 服务空间
        List<DtpRwdSpaceDTO> serviceSpaces = this.circuitIdAndServiceSpaces
                .getOrDefault(circuitId, Collections.emptyList());
        // 关联控制模块
        DtpRwdObjectDTO relController = this.circuitIdAndController.get(circuitId);
        // 功能类型参数
        InfoCodeParameters funcTypeParameter = InfoCodeParameters.ofConfig(
                this.infoCodeAndConfig.get(DtpRwdCircuitDTO.Fields.funcType), funcType);
        // 灯具功能类型参数
        InfoCodeParameters lampFuncTypeParameter = InfoCodeParameters.ofConfig(
                this.infoCodeAndConfig.get(DtpRwdCircuitDTO.Fields.lampFuncType), lampFuncType);

        // 重写空间名称
        for (DtpRwdSpaceDTO serviceSpace : serviceSpaces) {
            String dynamicName = concatSpaceFullName(serviceSpace,
                    this.buildingIdAndData.get(serviceSpace.getBuildingId()),
                    this.floorIdAndData.get(serviceSpace.getFloorId()));
            serviceSpace.setDynamicName(dynamicName);
        }
        BasicInfos basicInfo = new BasicInfos();
        basicInfo.setRelDistributionCabinet(relDistributionCabinet);
        basicInfo.setRelSpace(serviceSpaces);
        basicInfo.setRelController(relController);
        basicInfo.setFuncType(funcTypeParameter);
        basicInfo.setLampFuncType(lampFuncTypeParameter);
        return basicInfo;
    }

    /**
     * 拼接空间完整名称
     *
     * @param serviceSpace 服务空间
     * @param inBuilding   所在建筑
     * @param inFloor      所在楼层
     * @return 空间完整名称
     * @since 7.250101.0
     */
    private String concatSpaceFullName(
            @NonNull DtpRwdSpaceDTO serviceSpace,
            @Nullable DtpRwdBuildingDTO inBuilding,
            @Nullable DtpRwdFloorDTO inFloor) {
        String spaceLocalName = serviceSpace.getLocalName();
        // 将建筑ID和楼层ID拼接到服务空间的名称中
        StringJoiner joiner = new StringJoiner("/");
        if (Objects.nonNull(inBuilding) && Objects.nonNull(inBuilding.getLocalName())) {
            joiner.add(inBuilding.getLocalName());
        }
        if (Objects.nonNull(inFloor) && Objects.nonNull(inFloor.getLocalName())) {
            joiner.add(inFloor.getLocalName());
        }
        if (StringUtils.isNotBlank(spaceLocalName)) {
            joiner.add(spaceLocalName);
        }
        return joiner.toString();
    }

    /**
     * 根据信息点过滤照明回路
     *
     * @param circuits                 照明回路
     * @param circuitIdAndInfoCodeData 照明回路ID -> [信息点编码 -> 信息点值]
     * @return 过滤后的照明回路
     * @since 7.250101.0
     */
    private List<DtpRwdCircuitDTO> filterCircuitByInfoCode(
            @NonNull List<DtpRwdCircuitDTO> circuits,
            @NonNull Map<String, Map<String, InfoCodeDataModel>> circuitIdAndInfoCodeData) {
        if (CollectionUtils.isEmpty(circuits)) {
            return Lists.newArrayList();
        }
        String onlineStatus = param.getOnlineStatus();
        String manualAutoStatus = param.getManualAutoStatus();
        String runStatus = param.getRunStatus();
        String abnormalStatus = param.getAbnormalStatus();
        String orderFailAlarm = param.getOrderFailAlarm();

        // 所有信息点均为空时不过滤
        if (StringUtils.isAllBlank(onlineStatus, manualAutoStatus, runStatus, abnormalStatus, orderFailAlarm)) {
            return circuits;
        }
        // 存在过滤条件, 但无IOT时无法匹配
        if (MapUtils.isEmpty(circuitIdAndInfoCodeData)) {
            return Lists.newArrayList();
        }
        circuits.removeIf(circuit -> {
            String circuitId = circuit.getId();
            Map<String, InfoCodeDataModel> infoCodeAndData = circuitIdAndInfoCodeData.get(circuitId);
            if (MapUtils.isEmpty(infoCodeAndData)) {
                return true;
            }
            boolean onlineStatusNonMatch = StringUtils.isNotBlank(onlineStatus) && nonMatchInfoCodeValue(
                    infoCodeAndData.get(InfoCodeEnum.ONLINE_STATUS.value()), onlineStatus);
            boolean manualAutoStatusNonMatch = StringUtils.isNotBlank(manualAutoStatus) && nonMatchInfoCodeValue(
                    infoCodeAndData.get(InfoCodeEnum.MANUAL_AUTO_STATUS.value()), manualAutoStatus);
            boolean runStatusNonMatch = StringUtils.isNotBlank(runStatus) && nonMatchInfoCodeValue(
                    infoCodeAndData.get(InfoCodeEnum.RUN_STATUS.value()), runStatus);
            boolean abnormalStatusNonMatch = StringUtils.isNotBlank(abnormalStatus) && nonMatchInfoCodeValue(
                    infoCodeAndData.get(InfoCodeEnum.FAULT_STATUS.value()), abnormalStatus);
            boolean orderFailAlarmNonMatch = StringUtils.isNotBlank(orderFailAlarm) && nonMatchInfoCodeValue(
                    infoCodeAndData.get(InfoCodeEnum.ORDER_FAIL_ALARM.value()), orderFailAlarm);
            // 任何一个未匹配则删除
            return onlineStatusNonMatch | manualAutoStatusNonMatch | runStatusNonMatch
                    | abnormalStatusNonMatch | orderFailAlarmNonMatch;
        });
        return circuits;
    }

    /**
     * 是否不匹配信息点值, 当 {@code infoCodeDataMode} 为 {@literal null} 时返回 {@code false}
     *
     * @param infoCodeDataMode 信息点值模型
     * @param filterDataValue  过滤信息点值
     * @return 是否不匹配信息点值, 当 {@code infoCodeDataMode} 为 {@literal null} 时返回 {@code false}
     * @since 7.250101.0
     */
    private boolean nonMatchInfoCodeValue(
            @Nullable InfoCodeDataModel infoCodeDataMode,
            @NonNull String filterDataValue) {
        return Objects.nonNull(infoCodeDataMode) && !StringUtils.equals(
                filterDataValue, String.valueOf(infoCodeDataMode.getDataValue()));
    }
}
