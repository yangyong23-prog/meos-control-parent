package com.persagy.apccontrol.basic.structure.common.strategy.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.convert.DtpRwdConvert;
import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.entity.RcBasicsConfig;
import com.persagy.apccontrol.basic.model.enumtype.OnOffStateEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.system.DataTypeEnum;
import com.persagy.apccontrol.basic.model.param.airconditioning.ForceControlStatusEditParam;
import com.persagy.apccontrol.basic.model.param.airconditioning.ForceControlStatusQueryParam;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorEquipmentQueryParam;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorSpaceQueryParam;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.airconditioning.ForceControlStatusVO;
import com.persagy.apccontrol.basic.model.vo.instance.EquipmentVO;
import com.persagy.apccontrol.basic.outside.feign.model.dto.DtpTwinManualSend;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.feign.service.DtpTwinClientService;
import com.persagy.apccontrol.basic.outside.feign.service.PaasDataTransferClientService;
import com.persagy.apccontrol.basic.resource.RcBasicsConfigService;
import com.persagy.apccontrol.basic.resource.param.RcBasicsConfigQueryDaoParam;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.common.strategy.BaseRunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.common.strategy.RunningMonitorStrategy;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.enums.ResponseCode;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import com.persagy.validate.utility.enumeration.error.third.MiddlewareServerErrorEnum;
import com.persagy.validate.utility.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.text.Collator;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 空调冷源策略实现
 *
 * @author LiuYang
 * @since 7.250402.0
 */
@Validated
@Component(RunningMonitorAirConditioningColdSourceStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RunningMonitorAirConditioningColdSourceStrategy extends BaseRunningMonitorStrategy implements RunningMonitorStrategy {

    public static final String BEAN_NAME = "runningMonitorAirConditioningColdSourceStrategy";

    /**
     * 全量设备类
     */
    private static final List<DtpRwdClassCodeEnum> AIR_CONDITIONING_COLD_SOURCE_CLASS_CODES = Lists.newArrayList(
            DtpRwdClassCodeEnum.ACCCCC, DtpRwdClassCodeEnum.ACCCSC, DtpRwdClassCodeEnum.ACCCCP,
            DtpRwdClassCodeEnum.ACCCOP, DtpRwdClassCodeEnum.ACCCGP, DtpRwdClassCodeEnum.ACCCTP,
            DtpRwdClassCodeEnum.ACCCOF, DtpRwdClassCodeEnum.ACCCOT, DtpRwdClassCodeEnum.ACGEHE,
            DtpRwdClassCodeEnum.ACCCWT, DtpRwdClassCodeEnum.ACGEFP, DtpRwdClassCodeEnum.ACGEET,
            DtpRwdClassCodeEnum.ACGEST, DtpRwdClassCodeEnum.ACCCGT, DtpRwdClassCodeEnum.ACGECF,
            DtpRwdClassCodeEnum.ACCCCD, DtpRwdClassCodeEnum.ACGEDB, DtpRwdClassCodeEnum.ACGECL,
            DtpRwdClassCodeEnum.WESSTP, DtpRwdClassCodeEnum.WESSPM, DtpRwdClassCodeEnum.WESSFL,
            DtpRwdClassCodeEnum.WESSHS, DtpRwdClassCodeEnum.ACVVEO, DtpRwdClassCodeEnum.ACVVEJ,
            DtpRwdClassCodeEnum.ACCCST, DtpRwdClassCodeEnum.ACCCAH, DtpRwdClassCodeEnum.ACCCGP,
            DtpRwdClassCodeEnum.ACCCGH, DtpRwdClassCodeEnum.ACCCWH, DtpRwdClassCodeEnum.ACCCAC
    );

    /**
     * 空调工况设备类
     */
    private static final List<String> WORKING_CONDITION_CLASS_CODES = Lists.newArrayList(
            DtpRwdClassCodeEnum.ACCCCC.value(), DtpRwdClassCodeEnum.ACCCSC.value(),
            DtpRwdClassCodeEnum.ACCCGH.value(), DtpRwdClassCodeEnum.ACCCWH.value()
    );

    private static final List<String> DETECTOR_CLASS_CODES = Lists.newArrayList(
            DtpRwdClassCodeEnum.WESSTP.value(), DtpRwdClassCodeEnum.WESSWV.value(),
            DtpRwdClassCodeEnum.WESSCT.value(), DtpRwdClassCodeEnum.WESSFL.value(),
            DtpRwdClassCodeEnum.WESSFD.value(), DtpRwdClassCodeEnum.WESSTH.value(),
            DtpRwdClassCodeEnum.WESSCP.value(), DtpRwdClassCodeEnum.WESSCO.value(),
            DtpRwdClassCodeEnum.WESSTO.value(), DtpRwdClassCodeEnum.WESSPE.value(),
            DtpRwdClassCodeEnum.WESSIL.value(), DtpRwdClassCodeEnum.WESSCB.value(),
            DtpRwdClassCodeEnum.WESSWL.value(), DtpRwdClassCodeEnum.SPVSCM.value(),
            DtpRwdClassCodeEnum.CFCSDR.value(), DtpRwdClassCodeEnum.CFCSFR.value(),
            DtpRwdClassCodeEnum.CFCSED.value()
    );

    private final ObjectMapper objectMapper;
    private final DtpRwdClientService dtpRwdClientService;
    private final DtpKnowledgeClientService dtpKnowledgeClientService;
    private final RcBasicsConfigService rcBasicsConfigService;
    private final DtpRwdConvert dtpRwdConvert;
    private final DtpTwinClientService dtpTwinClientService;
    private final PaasDataTransferClientService paasDataTransferClientService;
    private final AbstractIotDataCacheServer abstractIotDataCacheServer;

    @Override
    public RunningMonitorTypeEnum runningMonitorType() {
        return RunningMonitorTypeEnum.AIR_CONDITIONING_COLD_SOURCE;
    }

    @Override
    public InheritConfigTypeEnum demandInheritType() {
        return InheritConfigTypeEnum.FIELD;
    }

    @Override
    public List<DtpRwdSpaceDTO> listSpaces(@NonNull @Valid RunningMonitorSpaceQueryParam param) {
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.addWithColumnWithSimple(DtpRwdObjectDTO.Fields.id);
        List<DtpRwdSpaceDTO> spaces = param.queryObject(queryParam);
        if (CollectionUtils.isEmpty(spaces)) {
            return Collections.emptyList();
        }
        // 过滤温度实例化的空间
        return spaces;
    }

    @Override
    public List<DtpRwdEquipmentDTO> listEquipments(@NonNull @Valid RunningMonitorEquipmentQueryParam param) {
        //查询系统下全部的设备
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam()
                .addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode);
        List<DtpRwdEquipmentDTO> airColdSourceEquipments =  param.queryObject(queryParam);

        //查询系统下全部的设备组
        param.setFilterEquipmentGroupInSystemIds(param.getFilterEquipmentInSystemIds());
        param.setFilterEquipmentInSystemIds(null);
        List<DtpRwdEquipmentDTO> airColdSourceEquipmentGroups = param.queryObject(queryParam);

        //过滤冷却塔控制单元下的设备
        if(CollectionUtil.isNotEmpty(airColdSourceEquipmentGroups)){
            Set<String> groupIds = airColdSourceEquipmentGroups.stream()
                    .filter(equipmentGroup -> "ACOTU".equals(equipmentGroup.getClassCode()))
                    .map(DtpRwdEquipmentDTO::getId)
                    .collect(Collectors.toSet());
            if(CollectionUtil.isNotEmpty(groupIds)){
                DtpRwdObjectQueryParam objectQueryParam = new DtpRwdObjectQueryParam();
                objectQueryParam.setRelationTo(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.EQUIP_IN_GROUP).setObjFrom(groupIds));
                objectQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
                List<DtpRwdEquipmentDTO> dtpRwdEquipmentDTOS = dtpRwdClientService.listEquipment(objectQueryParam);
                if(CollectionUtil.isNotEmpty(dtpRwdEquipmentDTOS)){
                    Map<String, DtpRwdEquipmentDTO> idMap = dtpRwdEquipmentDTOS.stream().collect(Collectors.toMap(DtpRwdEquipmentDTO::getId, Function.identity(), (v1, v2) -> v2));
                    airColdSourceEquipments.removeIf(equipment -> idMap.containsKey(equipment.getId()));
                }
            }
        }

        //本地远程状态(localRemoteStatus)=本地
        airColdSourceEquipments.addAll(airColdSourceEquipmentGroups);
        if(CollectionUtil.isNotEmpty(airColdSourceEquipments)){
            Set<String> objectIds = airColdSourceEquipments.stream().map(DtpRwdEquipmentDTO::getId).collect(Collectors.toSet());
            List<InfoCodeValueVO> infoCodeValues = abstractIotDataCacheServer
                    .listInfoCodeValueInOneHour(objectIds, Arrays.asList(InfoCodeEnum.LOCAL_REMOTZE_STATUS.value()));

            // 将信息点数组转为<对象id,<信息点,数值>>集合
            Map<String, Map<String, Object>> objectInfoCodeValueMap =
                    InfoCodeValueVO.listToObjectInfoCodeValueMap(infoCodeValues);
            airColdSourceEquipments.removeIf(item -> {
                Map<String, Object> itemValues = objectInfoCodeValueMap.get(item.getId());
                return itemValues != null && "0".equals(itemValues.get(InfoCodeEnum.LOCAL_REMOTZE_STATUS.value()));
            });
        }

        airColdSourceEquipments.sort(Comparator.comparing(DtpRwdEquipmentDTO::getLocalName,
            Comparator.nullsFirst(Collator.getInstance(Locale.CHINA))));
        return airColdSourceEquipments;
    }

    /**
     * 查询空调控制分区空间
     *
     * @param param 查询空调控制分区参数
     * @return 空调控制分区
     */
    public List<DtpRwdSpaceDTO> listACSpaces(@NonNull @Valid RunningMonitorSpaceQueryParam param) {
        if (CollectionUtils.isEmpty(param.getFilterSpaceServiceSystemIds())) {
            List<DtpRwdObjectDTO> systems = dtpRwdClientService.listObject(new DtpRwdObjectQueryParam()
                    .addWithColumn(DtpRwdObjectDTO.Fields.id)
                    .setClassCodeEnum(DtpRwdClassCodeEnum.ACAC_SYSTEM));
            param.setFilterSpaceServiceSystemIds(systems.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet()));
        }
        Set<String> withColumns = param.getWithColumns();
        DtpRwdObjectQueryParam spaceQueryParam = new DtpRwdObjectQueryParam();
        spaceQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
        if (CollectionUtils.isEmpty(withColumns)) {
            spaceQueryParam.addWithColumn(DtpRwdSpaceDTO.Fields.class);
        }
        List<DtpRwdSpaceDTO> systemServiceSpaces = param.queryObject(spaceQueryParam, dtpRwdClientService::listACSpace);
        String isTerminalPump = InfoCodeEnum.IS_TERMINAL_PUMP.value();
        String netUseType = InfoCodeEnum.NET_USE_TYPE.value();
        List<DtpRwdEquipmentDTO> dtpRwdEquipments = dtpRwdClientService.listEquipment(new DtpRwdObjectQueryParam()
                .addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode, netUseType, isTerminalPump)
                .setClassCodeEnum(Lists.newArrayList(DtpRwdClassCodeEnum.ACVVEO, DtpRwdClassCodeEnum.ACCPC_GROUP)));
        if (CollectionUtils.isNotEmpty(dtpRwdEquipments)) {
            dtpRwdEquipments.removeIf(equipment ->
                    !Objects.equals(equipment.getClassCode(), DtpRwdClassCodeEnum.ACVVEO.value())
                            && !(Objects.equals(equipment.getClassCode(), DtpRwdClassCodeEnum.ACCPC_GROUP.value())
                            && Objects.equals(equipment.analyzeProperties(netUseType), "2")
                            && Objects.equals(equipment.analyzeProperties(isTerminalPump), "1"))
            );
        }
        List<DtpRwdSpaceDTO> equipmentServiceSpaces = new ArrayList<>();
        Set<String> equipmentIds = dtpRwdEquipments.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(equipmentIds)) {
            spaceQueryParam.setRelationTo(new DtpRwdObjectRelationQueryParam(
                    DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE).setObjFrom(equipmentIds));
            equipmentServiceSpaces = dtpRwdClientService.listACSpace(spaceQueryParam);
        }

        return deduplicateById(systemServiceSpaces, equipmentServiceSpaces, DtpRwdObjectDTO::getId);
    }

    public ForceControlStatusVO getForceControlStatus(@NonNull @Valid ForceControlStatusQueryParam param) {
        String objectId = param.getObjectId();

        ForceControlStatusVO result = new ForceControlStatusVO();
        RcBasicsConfig config = getForceControlStatusConfig(objectId);
        result.setStatus(Objects.nonNull(config)
                ? BaseEnum.analyze(config.getResult(), OnOffStateEnum.class)
                : OnOffStateEnum.CLOSE);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean editForceControlStatus(@NonNull @Valid ForceControlStatusEditParam param) {
        String objectId = param.getObjectId();
        OnOffStateEnum status = param.getStatus();
        List<ForceControlStatusEditParam.ReleaseForceControlInfoCode> releaseForceControlInfoCodes = param.getReleaseForceControlInfoCodes();

        RcBasicsConfig config = getForceControlStatusConfig(objectId);
        switch (status) {
            case OPEN:
                DataTypeEnum dataType = DataTypeEnum.ADD;
                RcBasicsConfig addConfig = new RcBasicsConfig();
                if (Objects.nonNull(config)) {
                    addConfig.setId(config.getId());
                    dataType = DataTypeEnum.EDIT;
                }
                addConfig.setProjectId(ContextUtil.getProjectId());
                addConfig.setType(BasicsConfigTypeEnum.FORCE_CONTROL_STATUS);
                addConfig.setFacilityCode(objectId);
                addConfig.setResult(status.value());
                rcBasicsConfigService.saveOrUpdateBasicsConfig(addConfig);
                paasDataTransferClientService.syncDbDataToCloud(
                        RcBasicsConfig.TABLE_NAME, null,
                        dataType, objectMapper.convertValue(addConfig, ObjectNode.class));
                break;
            case CLOSE:
                // 存在解除强制控制信息点时, 进行解除强控
                if (CollectionUtils.isNotEmpty(releaseForceControlInfoCodes)) {
                    List<CompletableFuture<Void>> futures = new ArrayList<>();
                    for (ForceControlStatusEditParam.ReleaseForceControlInfoCode releaseForceControlInfoCode : releaseForceControlInfoCodes) {
                        if (CollectionUtils.isNotEmpty(releaseForceControlInfoCode.getInfoCodes())) {
                            for (String infoCode : releaseForceControlInfoCode.getInfoCodes()) {
                                futures.add(CompletableFuture.runAsync(() -> dtpTwinClientService.manualSend(
                                                DtpTwinManualSend.builder()
                                                        .objectId(releaseForceControlInfoCode.getObjectId())
                                                        .infoCode(infoCode)
                                                        .value(CommonConst.FORCE_VALUE).build()),
                                        BusinessThreadPoolExecutor.ttlExecutor()));
                            }
                        }
                    }
                    try {
                        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
                    } catch (InterruptedException | ExecutionException e) {
                        // 固定产品提示文案
                        String errorMessage = LocaleMessageUtil.getMessage("key_8b44afbaaf794b7a9e559f337035b3d9");
                        logProjectInstance().error(errorMessage, e, Pair.of("param", param));
                        throw new ApplicationException(MiddlewareServerErrorEnum.RPC_SERVER_ERROR.value(),
                                e.getLocalizedMessage(), errorMessage);
                    }
                }
                if (Objects.nonNull(config)) {
                    rcBasicsConfigService.deleteById(config);
                    paasDataTransferClientService.syncDbDataToCloud(
                            RcBasicsConfig.TABLE_NAME, null,
                            DataTypeEnum.DELETE, objectMapper.convertValue(config, ObjectNode.class));
                }
                break;
        }
        return true;
    }

    private RcBasicsConfig getForceControlStatusConfig(@NonNull String objectId) {
        RcBasicsConfigQueryDaoParam rcBasicsConfigQueryDaoParam = new RcBasicsConfigQueryDaoParam();
        rcBasicsConfigQueryDaoParam.setProjectId(ContextUtil.getProjectId());
        rcBasicsConfigQueryDaoParam.setFacilityCode(objectId);
        rcBasicsConfigQueryDaoParam.setType(BasicsConfigTypeEnum.FORCE_CONTROL_STATUS);
        return rcBasicsConfigService.getValid(rcBasicsConfigQueryDaoParam);
    }

    /**
     * 根据id去重
     *
     * @param systemServiceSpaces    系统服务空间
     * @param equipmentServiceSpaces 设备服务空间
     * @param idExtractor            字段
     * @return 去重后的集合
     */
    private <T> List<T> deduplicateById(List<T> systemServiceSpaces, List<T> equipmentServiceSpaces,
                                        Function<T, Object> idExtractor) {
        List<T> combined = new ArrayList<>(systemServiceSpaces);
        combined.addAll(equipmentServiceSpaces);

        // 根据ID去重
        return new ArrayList<>(combined.stream()
                .collect(Collectors.toMap(
                        idExtractor,
                        item -> item,
                        (existing, replacement) -> existing  // 保留第一个出现的元素
                ))
                .values());
    }

    /**
     * 查询模型视图设备列表
     *
     * @param param 参数
     * @param type  类型 SYSTEM_RELATED_EQUIPMENT:系统关联的设备,EQUIPMENT_IN_SYSTEM:系统下的设备 EQUIPMENT_IN_SPACE:机房内的全部设备
     * @return {@code List<DtpRwdEquipmentDTO> }
     * @since 7.250602.0
     */
    public Set<EquipmentVO> listEquipmentsToModuleView(@NonNull @Valid RunningMonitorEquipmentQueryParam param, String type) {
        List<DtpRwdEquipmentDTO> equipmentList;
        switch (type) {
            case "SYSTEM_RELATED_EQUIPMENT":
                equipmentList = buildSystemRelatedEquipments(param);
                break;
            case "EQUIPMENT_IN_SYSTEM":
                equipmentList = buildSystemInEquipments(param);
                break;
            case "EQUIPMENT_IN_SPACE":
                if (CollectionUtils.isEmpty(param.getFilterEquipmentInSpaceIds())) {
                    throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_14bab60fb064469699089d23a9d3ef55"), LocaleMessageUtil.getMessage("key_14bab60fb064469699089d23a9d3ef55"));
                }
                equipmentList = param.queryObject(new DtpRwdObjectQueryParam().addWithColumn(DtpRwdEquipmentDTO.listSimpleWithColumns()));
                break;
            default:
                return Collections.emptySet();
        }
        Set<EquipmentVO> equipmentVos = Collections.emptySet();
        if (CollectionUtils.isNotEmpty(equipmentList)) {
            equipmentVos = equipmentList.stream()
                    .map(dtpRwdConvert::toEquipmentVo)
                    .collect(Collectors.toSet());
            Set<String> classCodes = equipmentList.stream().map(DtpRwdEquipmentDTO::getClassCode).collect(Collectors.toSet());
            Map<String, String> classCodeAndNameMap = dtpKnowledgeClientService.mapClassCodeAndName(true, false, classCodes);
            equipmentVos.forEach(equipment -> equipment.setClassCodeName(classCodeAndNameMap.get(equipment.getClassCode())));
        }
        return equipmentVos;
    }


    private List<DtpRwdEquipmentDTO> buildSystemInEquipments(@NonNull @Valid RunningMonitorEquipmentQueryParam param) {

        if (CollectionUtils.isEmpty(param.getFilterEquipmentInSpaceIds()) || CollectionUtils.isEmpty(param.getFilterEquipmentInSystemIds())) {
            throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_875fcdaf5f3c4f599dd64549fc090773"), LocaleMessageUtil.getMessage("key_875fcdaf5f3c4f599dd64549fc090773"));
        }
        //先查询系统下的设备&空间下的设备的交集。
        List<DtpRwdEquipmentDTO> dtpRwdEquipmentDTOS = param.queryObject(new DtpRwdObjectQueryParam().addWithColumn(DtpRwdEquipmentDTO.listSimpleWithColumns()));
        //再查询探测器设备
        List<DtpRwdEquipmentDTO> detectorEquipments = this.listDetectorEquipments(param);
        if (CollectionUtils.isNotEmpty(detectorEquipments)) {
            dtpRwdEquipmentDTOS.addAll(detectorEquipments);
        }
        return dtpRwdEquipmentDTOS;
    }

    /**
     * 构建系统相关设备
     *
     * @param param 参数
     * @return {@code List<DtpRwdEquipmentDTO> }
     * @since 7.250602.0
     */
    private List<DtpRwdEquipmentDTO> buildSystemRelatedEquipments(@NonNull @Valid RunningMonitorEquipmentQueryParam param) {
        if (CollectionUtils.isEmpty(param.getFilterEquipmentInSpaceIds())) {
            throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_14bab60fb064469699089d23a9d3ef55"), LocaleMessageUtil.getMessage("key_14bab60fb064469699089d23a9d3ef55"));
        }
        //主要管理对象：空调专业的设备&空间下的设备的交集。
        //1、空调专业的设备：查询到空调专业（AC）下的设备，过滤掉分体空调系统（ACSA）、空调末端系统（ACAT）、通风系统（ACVT）系统下的设备。
        //2、空间下的设备：根据设备所在空间（MechInArch / Eq2Sp）关系查询。
        //先查询空间下空调专业的设备
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.addInfoCode(InfoCodeEnum.MAJOR.getCode(), "AC");
        queryParam.addWithColumn(DtpRwdEquipmentDTO.listSimpleWithColumns());
        List<DtpRwdEquipmentDTO> result = param.queryObject(queryParam);
        //再查询需要过滤系统的设备
        DtpRwdObjectQueryParam systemParam = new DtpRwdObjectQueryParam();
        systemParam.setClassCode(Arrays.asList(DtpRwdClassCodeEnum.ACSA_SYSTEM.getValue(), DtpRwdClassCodeEnum.ACAT_SYSTEM.getValue(), DtpRwdClassCodeEnum.ACVT_SYSTEM.getValue()));
        systemParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
        //拿到需要排除的系统id
        Set<String> excludeSystemIds = dtpRwdClientService.listObject(systemParam).stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(excludeSystemIds)) {
            Set<Object> equipmentIds = result.stream().map(DtpRwdEquipmentDTO::getId).collect(Collectors.toSet());
            DtpRwdObjectQueryParam excludeEquipmentParam = new DtpRwdObjectQueryParam();
            excludeEquipmentParam.setId(equipmentIds);
            excludeEquipmentParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
            //拿到需要排除的设备id
            Set<String> excludeEquipmentIds = dtpRwdClientService.listEquipmentInBySystemIds(excludeSystemIds, excludeEquipmentParam).stream().map(DtpRwdEquipmentDTO::getId).collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(excludeEquipmentIds)) {
                result = result.stream().filter(item -> !excludeEquipmentIds.contains(item.getId())).collect(Collectors.toList());
            }
        }
        //查询服务于机房的重要探测器
        List<DtpRwdEquipmentDTO> detectorEquipments = this.listDetectorEquipments(param);
        if (CollectionUtils.isNotEmpty(detectorEquipments)) {
            result.addAll(detectorEquipments);
        }
        return result;
    }

    /**
     * 获取探测器设备列表
     *
     * @return {@code List<DtpRwdEquipmentDTO> }
     * @since 7.250602.0
     */
    private List<DtpRwdEquipmentDTO> listDetectorEquipments(RunningMonitorEquipmentQueryParam param) {
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.setClassCode(DETECTOR_CLASS_CODES);
        queryParam.addWithColumn(DtpRwdEquipmentDTO.listSimpleWithColumns());
        //这里只查询空间下的设备，所以需要把系统设置为null
        param.setFilterEquipmentInSystemIds(null);
        return param.queryObject(queryParam);
    }

    @Override
    public String logTag() {
        return "运行检测-空调冷源";
    }
}
