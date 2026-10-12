package com.persagy.apccontrol.basic.abstracts;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.persagy.apccontrol.basic.handler.LocalRedisCacheHandler;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.EnergyDataInfoQueryParam;
import com.persagy.apccontrol.basic.model.dto.EnergyDataQueryParam;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFractionalPrecisionRuleDTO;
import com.persagy.apccontrol.basic.model.entity.EquipRecord;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.enumtype.SourceFromEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdPersistenceTimeTypeEnum;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.outside.dto.DtpPointInfo;
import com.persagy.apccontrol.basic.outside.dto.EnergyDataResponse;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdTwinObjectRuleByIdQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.ObjectInstantiateQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.rwd.DtpRwdTwinObjectRuleByIdResponse;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.BizComponent;
import com.persagy.apccontrol.basic.service.BizDtpRwdService;
import com.persagy.apccontrol.basic.service.EquipRecordService;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.iot.IotConverter;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.executor.MapExecutor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author shaohongbo
 * @date 2023/4/4 19:12
 */
public abstract class AbstractIotDataCacheServer<T> implements BizComponent<AbstractIotDataCacheServer<T>> {

    @Autowired
    protected LocalRedisCacheHandler localRedisCacheHandler;
    @Lazy
    @Autowired
    private AbstractCommonServer abstractCommonServer;
    @Autowired
    private BizDtpRwdService bizDtpRwdService;
    @Autowired
    private DtpRwdClientService dtpRwdClientService;
    @Autowired
    private AbstractDtpPersistServer abstractDtpPersistServer;
    @Autowired
    private IotConverter iotConverter;
    @Autowired
    private EquipRecordService equipRecordService;

    public abstract String queryIotRealData(String groupCode, String projectId, String objectId, RcInfoCodeCfg infoCodeCfg);

    public abstract String queryIotRealData(String groupCode, String projectId, String objectId, String classCode, String infoCode);

    public abstract T queryIotRealDataVO(String groupCode, String projectId, String objectId, RcInfoCodeCfg infoCodeCfg);

    public abstract T queryIotRealDataVO(String groupCode, String projectId, String objectId, String classCode, String infoCode);

    public abstract T queryIotCtrlDataVO(String groupCode, String projectId, String objectId, String classCode, String infoCode);

    /**
     * 从 Redis 缓存中查询实时反馈值（包含开启中、关闭中等中间态信息点）
     *
     * @param objectId 对象ID
     * @param infoCode 信息点编码
     * @return Redis 缓存中的信息点数据，不存在时返回 null
     * @since 7.260402.0
     */
    public abstract T queryIotRealDataRedisVO(String objectId, String infoCode);

    public abstract List<T> queryIotRealDataList(String groupCode, String projectId, String objectId, List<RcInfoCodeCfg> infoCodeCfgs);

    /**
     * @param groupCode
     * @param projectId
     * @param objectId
     * @param infoCodes
     * @return objectId: infoCode: infoValue
     */
    public abstract Map<String, Map<String, String>> queryIotRealDataMap(String groupCode, String projectId, Set<String> objectId, Set<String> infoCodes);

    /**
     * 注意边缘端的响应，边缘端只返回 DtpPointInfo(String objectId, String infoCode, String dataValue, String dataTime)
     *
     * @param groupCode
     * @param projectId
     * @param objectId
     * @param infoCodeCfgs
     * @return
     */
    public abstract List<DtpPointInfo> queryCommonIotRealDataList(String groupCode, String projectId, String objectId, List<RcInfoCodeCfg> infoCodeCfgs);

    public abstract List<T> queryIotRealDataList(String groupCode, String projectId, String objectId, String classCode, List<String> infoCodes);

    // ------------------------- 查询孪生规则 -------------------------

    /**
     * 查询对象ID和存在规则信息点编码映射
     *
     * @param classCodeAndObjectIds 设备类编码 -> 对象ID
     * @param infoCodes             信息点编码
     * @return 对象ID -> 存在规则信息点编码
     * @since 5.241102.0
     */
    public Map<String, Set<String>> mapObjectIdAndExistsRuleInfoCodes(
            @NonNull Map<String, Set<String>> classCodeAndObjectIds,
            @NonNull Collection<String> infoCodes) {
        return mapObjectIdAndExistsRuleInfoCodes(ContextUtil.getProjectId(), classCodeAndObjectIds, infoCodes);
    }

    /**
     * 查询对象ID和云端存在规则信息点编码映射
     *
     * @param classCodeAndObjectIds 设备类编码 -> 对象ID
     * @param infoCodes             信息点编码
     * @return 对象ID -> 云端存在规则信息点编码
     * @since 7.250602.0
     */
    public Map<String, Set<String>> mapObjectIdAndExistsRuleInfoCodesByCloud(
            @NonNull Map<String, Set<String>> classCodeAndObjectIds,
            @NonNull Collection<String> infoCodes) {
        if (MapUtils.isEmpty(classCodeAndObjectIds) || CollectionUtils.isEmpty(infoCodes)) {
            return Maps.newHashMap();
        }
        List<ObjectInstantiateQueryParam> queryParams = new ArrayList<>();
        for (Set<String> objectIds : classCodeAndObjectIds.values()) {
            for (String objectId : objectIds) {
                for (String infoCode : infoCodes) {
                    queryParams.add(ObjectInstantiateQueryParam.batchInstance(objectId, infoCode));
                }
            }
        }
        return dtpRwdClientService.mapObjectIdAndInstantiateInfoCodes(ContextUtil.getProjectId(), queryParams);
    }

    /**
     * 查询对象ID和云端存在规则信息点编码映射
     *
     * @param classCodeAndObjectIds 设备类编码 -> 对象ID
     * @param infoCodes             信息点编码
     * @return 对象ID -> 云端存在规则信息点编码
     * @since 7.250602.0
     */
    public Map<String, Set<String>> mapObjectIdAndExistsRuleInfoCodesByEdge(
            @NonNull Map<String, Set<String>> classCodeAndObjectIds,
            @NonNull Collection<String> infoCodes) {
        if (MapUtils.isEmpty(classCodeAndObjectIds) || CollectionUtils.isEmpty(infoCodes)) {
            return Maps.newHashMap();
        }
        Set<String> allObjectIds = classCodeAndObjectIds.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        DtpRwdTwinObjectRuleByIdQueryParam queryParam = new DtpRwdTwinObjectRuleByIdQueryParam();
        queryParam.setObjectIds(allObjectIds);
        return dtpRwdClientService.listTwinObjectRuleById(queryParam).stream()
                .filter(item -> infoCodes.contains(item.getInfoCode()))
                .collect(Collectors.groupingBy(DtpRwdTwinObjectRuleByIdResponse::getObjectId,
                        Collectors.mapping(DtpRwdTwinObjectRuleByIdResponse::getInfoCode, Collectors.toSet())));
    }

    /**
     * 查询对象ID和存在规则信息点编码映射
     *
     * @param projectId             项目ID
     * @param classCodeAndObjectIds 设备类编码 -> 对象ID
     * @param infoCodes             信息点编码
     * @return 对象ID -> 存在规则信息点编码
     * @since 5.240701.0
     */
    public abstract Map<String, Set<String>> mapObjectIdAndExistsRuleInfoCodes(
            @NonNull String projectId,
            @NonNull Map<String, Set<String>> classCodeAndObjectIds,
            @NonNull Collection<String> infoCodes);

    // ------------------------- 查询IOT实时数据 -------------------------

    /**
     * 查询当前信息点数据
     *
     * @param projectId 项目ID
     * @param classCode 设备类编码, 为空时不查询信息点定义
     * @param objectId  对象ID
     * @param infoCode  信息点编码
     * @return 最近60分钟信息点数据
     * @since 5.240502.0
     */
    public InfoCodeValueVO getInfoCodeValueBy60Minute(
            @NonNull String projectId, @Nullable DtpRwdClassCodeEnum classCode,
            @NonNull String objectId, @NonNull String infoCode) {
        return getOne(listInfoCodeValue(projectId, classCode,
                Lists.newArrayList(objectId), Lists.newArrayList(infoCode)));
    }

    /**
     * 查询当前信息点数据
     *
     * @param projectId 项目ID
     * @param classCode 设备类编码, 为空时不查询信息点定义
     * @param objectId  对象ID
     * @param infoCodes 信息点编码
     * @return 最近60分钟信息点数据
     * @since 5.240502.0
     */
    public List<InfoCodeValueVO> listInfoCodeValue(
            @NonNull String projectId, @Nullable DtpRwdClassCodeEnum classCode,
            @NonNull String objectId, @NonNull Collection<String> infoCodes) {
        return listInfoCodeValue(projectId, classCode, Lists.newArrayList(objectId), infoCodes);
    }

    /**
     * 查询当前信息点数据
     *
     * @param projectId 项目ID
     * @param classCode 设备类编码, 为空时不查询信息点定义
     * @param objectIds 对象ID
     * @param infoCode  信息点编码
     * @return 最近60分钟信息点数据
     * @since 5.240502.0
     */
    public List<InfoCodeValueVO> listInfoCodeValue(
            @NonNull String projectId, @Nullable DtpRwdClassCodeEnum classCode,
            @NonNull Collection<String> objectIds, @NonNull String infoCode) {
        return listInfoCodeValue(projectId, classCode, objectIds, Lists.newArrayList(infoCode));
    }

    /**
     * 查询最近60分钟信息点数据
     *
     * @param projectId 项目ID
     * @param classCode 设备类编码, 为空时不查询信息点定义
     * @param objectIds 对象ID
     * @param infoCodes 信息点编码
     * @return 最近60分钟信息点数据
     * @since 5.240502.0
     */
    public abstract List<InfoCodeValueVO> listInfoCodeValue(
            @NonNull String projectId, @Nullable DtpRwdClassCodeEnum classCode,
            @NonNull Collection<String> objectIds, @NonNull Collection<String> infoCodes);

    /**
     * 查询最近60分钟信息点数据
     *
     * @param objectIds 对象ID
     * @param infoCodes 信息点编码
     * @return 最近60分钟信息点数据
     * @since 5.240502.0
     */
    public abstract List<InfoCodeValueVO> listInfoCodeValueInOneHour(
            @NonNull Collection<String> objectIds, @NonNull Collection<String> infoCodes);

    /**
     * 查询云端信息点数据
     *
     * @param objectIds 对象ID
     * @param infoCodes 信息点编码
     * @return 云端信息点数据
     * @since 7.250602.0
     */
    public List<InfoCodeValueVO> listInfoCodeValue1dByCloud(
            @NonNull Set<String> objectIds,
            @NonNull Set<String> infoCodes) {
        return dtpRwdClientService.queryIpDataOnMoment(objectIds, infoCodes, DtpRwdPersistenceTimeTypeEnum.DAY, null, null)
                .stream().map(iotConverter::toInfoCodeValueVo).collect(Collectors.toList());
    }

    /**
     * 查询云端信息点数据
     *
     * @param ipType    精度
     * @param objectIds 对象ID
     * @param infoCodes 信息点编码
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 云端信息点数据
     * @since 7.250602.0
     */
    public List<InfoCodeValueVO> listInfoCodeValueByCloud(
            @NonNull DtpRwdPersistenceTimeTypeEnum ipType,
            @NonNull Collection<String> objectIds,
            @NonNull Collection<String> infoCodes,
            @Nullable LocalDateTime startTime,
            @Nullable LocalDateTime endTime) {
        if (CollectionUtils.isEmpty(objectIds) || CollectionUtils.isEmpty(infoCodes)) {
            return Collections.emptyList();
        }
        EnergyDataQueryParam energyDataQueryParam = new EnergyDataQueryParam();
        energyDataQueryParam.setObjects(objectIds.stream().map(objectId -> {
            EnergyDataInfoQueryParam param = new EnergyDataInfoQueryParam();
            param.setObjectId(objectId);
            param.setInfoCodes(infoCodes);
            return param;
        }).collect(Collectors.toList()));
        energyDataQueryParam.setIpType(ipType.value());
        energyDataQueryParam.setStartTime(startTime != null ? startTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) : null);
        energyDataQueryParam.setEndTime(endTime != null ? endTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) : null);
        energyDataQueryParam.setDataStatus(1);
        return abstractDtpPersistServer.listIpDataInPeriod(energyDataQueryParam).stream()
                .map(iotConverter::toInfoCodeValueVo)
                .collect(Collectors.toList());
    }

    /**
     * 查询信息点精度，目前仅实现云端，入参不支持信息点仅，响应回来加一层过滤
     *
     * @param objectIds 对象ID
     * @param infoCodes 信息点编码
     * @return 信息点精度
     * @since 7.250102.0
     */
    public abstract List<DtpRwdFractionalPrecisionRuleDTO> queryInfoPointPeroid(
            @NonNull Set<String> objectIds, @Nullable Set<String> infoCodes);

    /**
     * 批量查询设备的信息点数据
     *
     * @param projectId 项目ID
     * @param objectIds 对象ID
     * @param infoCodes 信息点
     * @return 设备的信息点数据
     */
    public List<T> queryIotRealDataListByList(
            String projectId, Collection<String> objectIds, Collection<String> infoCodes) {
        return queryIotRealDataListByList(ContextUtil.getGroupCode(), projectId, objectIds, infoCodes);
    }

    // ------------------------- 查询IOT实时数据 -------------------------

    /**
     * 批量查询设备的信息点数据
     *
     * @param groupCode    集团编码-用于云端查rwd得iot信息
     * @param projectId    项目id-用于云端查rwd得iot信息
     * @param objectIdList 对象id集合
     * @param infoCodeList 信息点集合
     * @return
     */
    public abstract List<T> queryIotRealDataListByList(
            String groupCode, String projectId,
            Collection<String> objectIdList, Collection<String> infoCodeList);

    /**
     * 查询对象IOT设定信息点恢复时间
     *
     * <p>临时放到这里, 后期需要优化
     *
     * @param aggregates 聚合根
     * @return 对象ID -> [信息点编码 -> 恢复时间]
     * @since 5.240602.0
     */
    public Map<String, Map<String, LocalDateTime>> queryObjectIdIotSetInfoCodeAndRecoverTime(
            @NonNull Collection<InfoCodeAggregate> aggregates) {
        if (CollectionUtils.isEmpty(aggregates)) {
            return Maps.newHashMap();
        }
        Set<String> allIotSetInfoCodes = new HashSet<>();
        Map<String, Set<String>> objectIdAndIotSetInfoCodes = new LinkedHashMap<>();
        for (InfoCodeAggregate aggregate : aggregates) {
            String objectId = aggregate.getIdentifier().getObject().getId();
            InfoCodeTypeEnum infoCodeType = aggregate.getConfigModel().getInfoCodeType();
            String infoCode = aggregate.getConfigModel().getInfoCode();

            if (infoCodeType == InfoCodeTypeEnum.IOT_SETTING) {
                allIotSetInfoCodes.add(infoCode);
                MapExecutor.dynamicPut(objectIdAndIotSetInfoCodes, objectId, infoCode, HashSet::new);
            }
        }
        // 仅设定信息点才查干预截止时间
        if (CollectionUtils.isEmpty(allIotSetInfoCodes)) {
            return Maps.newHashMap();
        }

        Set<String> objectIds = objectIdAndIotSetInfoCodes.keySet();
        Map<String, Map<String, LocalDateTime>> result = new LinkedHashMap<>();

        // 单独查询手自动设定恢复时间
        if (allIotSetInfoCodes.contains(InfoCodeEnum.MANUAL_AUTO_SET.getCode())) {
            Map<String, LocalDateTime> objectIdAndManualAutoSetRecoverTime =
                    queryObjectIdAndManualAutoSetRecoverTime(objectIds);
            objectIdAndManualAutoSetRecoverTime.forEach((objectId, manualAutoSetRecoverTime) ->
                    MapExecutor.dynamicPut(result, objectId, InfoCodeEnum.MANUAL_AUTO_SET.getCode(),
                            manualAutoSetRecoverTime, HashMap::new));
            allIotSetInfoCodes.remove(InfoCodeEnum.MANUAL_AUTO_SET.getCode());
        }

        // 查询非手自动设定外其他设定信息点恢复时间
        if (CollectionUtils.isNotEmpty(allIotSetInfoCodes)) {
            Map<String, Map<String, LocalDateTime>> objectIdIotSettingAndRecoverTime =
                    queryObjectIdIotSettingAndRecoverTime(objectIds, allIotSetInfoCodes);
            objectIdIotSettingAndRecoverTime.forEach((objectId, iotSettingANdRecoverTime) -> {
                iotSettingANdRecoverTime.forEach((infoCode, recoverTime) -> {
                    MapExecutor.dynamicPut(result, objectId, infoCode, recoverTime, HashMap::new);
                });
            });
        }
        return result;
    }

    /**
     * 查询对象IOT设定信息点恢复时间
     *
     * <p>临时放到这里, 后期需要优化
     *
     * @param objectIds 对象ID
     * @return 对象ID -> [信息点编码 -> 恢复时间]
     * @since 5.240702.0
     */
    public Map<String, Map<String, LocalDateTime>> queryObjectIdAllIotSetInfoCodeAndRecoverTime(
            @NonNull Collection<String> objectIds) {
        Map<String, Map<String, LocalDateTime>> result = new LinkedHashMap<>();

        // 单独查询手自动设定恢复时间
        Map<String, LocalDateTime> objectIdAndManualAutoSetRecoverTime =
                queryObjectIdAndManualAutoSetRecoverTime(objectIds);
        objectIdAndManualAutoSetRecoverTime.forEach((objectId, manualAutoSetRecoverTime) ->
                MapExecutor.dynamicPut(result, objectId, InfoCodeEnum.MANUAL_AUTO_SET.getCode(),
                        manualAutoSetRecoverTime, HashMap::new));

        // 查询非手自动设定外其他设定信息点恢复时间
        Map<String, Map<String, LocalDateTime>> objectIdIotSettingAndRecoverTime =
                queryObjectIdIotSettingAndRecoverTime(objectIds, null);
        objectIdIotSettingAndRecoverTime.forEach((objectId, iotSettingANdRecoverTime) -> {
            iotSettingANdRecoverTime.forEach((infoCode, recoverTime) -> {
                MapExecutor.dynamicPut(result, objectId, infoCode, recoverTime, HashMap::new);
            });
        });
        return result;
    }

    /**
     * iot历史数据 云端24小时，边缘端2小时
     *
     * @param objectIds    对象ID
     * @param infoCodeList 信息点数组
     * @return 对象ID -> [信息点编码 -> 数值]
     * @since 5.240702.0
     */
    public abstract Map<String, Map<String, Map<String, String>>> queryObjectIdInfoCodeHistory(
            @NonNull DtpRwdPersistenceTimeTypeEnum ipType,
            @NonNull Collection<String> objectIds,
            @NonNull Collection<String> infoCodeList);

    /**
     * 查询持久化数据
     *
     * @param ipType       时间类型
     * @param objectIds    对象id
     * @param infoCodeList 信息点集合
     * @param isReplace
     * @return 持久化结果集
     */
    public List<InfoCodeValueVO> queryObjectIdPersistentDataList(@NonNull DtpRwdPersistenceTimeTypeEnum ipType,
                                                                 @NonNull Collection<String> objectIds,
                                                                 @NonNull Collection<String> infoCodeList,
                                                                 @Nullable String startTime,
                                                                 @Nullable String endTime, Boolean isReplace) {

        if (CollectionUtils.isEmpty(objectIds) || CollectionUtils.isEmpty(infoCodeList)) {
            return Lists.newArrayList();
        }

        EnergyDataQueryParam bodyDto = abstractCommonServer.getRwdInfo(new ArrayList<>(objectIds), new ArrayList<>(infoCodeList));
        Map<String, Object> extra = ContextUtil.getContext().getExtra();
        if (CommonConst.isNeedReplacedSystem(extra) && isReplace) {
            startTime = DateUtils.transferDateFormat(extra.get(CommonConst.IPDATA_START_TIME).toString(),
                    DateUtils.SDF_SECOND, DateUtils.SDFSECOND);
            endTime = DateUtils.transferDateFormat(extra.get(CommonConst.IPDATA_END_TIME).toString(),
                    DateUtils.SDF_SECOND, DateUtils.SDFSECOND);
            ipType = DtpRwdPersistenceTimeTypeEnum.analyze(extra.get(CommonConst.IPTYPE).toString());
            bodyDto.setDataStatus(1);
        }

        // 将时间设为当天的 0 点
        bodyDto.setStartTime(startTime);
        bodyDto.setEndTime(endTime);
        bodyDto.setIpType(ipType.value());
        // iot数据查询
        List<EnergyDataResponse> energyDataResponses = this.bizDtpRwdService.queryIpDataInPeriod(bodyDto);

        if (CollectionUtils.isNotEmpty(energyDataResponses)) {
            List<InfoCodeValueVO> result = new ArrayList<>();
            for (EnergyDataResponse dataResponse : energyDataResponses) {
                result.add(iotConverter.toInfoCodeValueVo(dataResponse));
            }
            return result;
        }
        return Lists.newArrayList();
    }

    /**
     * 查询持久化数据
     *
     * @param ipType       时间类型
     * @param objectIds    对象id
     * @param infoCodeList 信息点集合
     * @return 按对象id，再按信息点，再按时间，最后的value是数值方便取值
     */
    public Map<String, Map<String, Map<LocalDateTime, Object>>> queryObjectIdPersistentDataMap(DtpRwdPersistenceTimeTypeEnum ipType,
                                                                                               @NonNull Collection<String> objectIds,
                                                                                               @NonNull Collection<String> infoCodeList) {

        String startTime = DateUtils.getStartTimeOfDayString(DateUtils.getDateNow());
        String endTime = DateUtils.getNowTimeString();
        // iot数据查询
        List<InfoCodeValueVO> ipDataList = queryObjectIdPersistentDataList(ipType, objectIds, infoCodeList, startTime, endTime, true);
        if (CollectionUtils.isNotEmpty(ipDataList)) {
            // 按对象id，再按信息点，再按时间，最后的value是数值方便取值
            return ipDataList.stream()
                    .collect(Collectors.groupingBy(
                                    InfoCodeValueVO::getObjectId,
                                    Collectors.groupingBy(
                                            InfoCodeValueVO::getInfoCode,
                                            Collectors.toMap(
                                                    InfoCodeValueVO::getUpdateTime,
                                                    item -> String.valueOf(item.getInfoCodeValue())
                                            )
                                    )
                            )
                    );
        }
        return new HashMap<>(16);

    }

    /**
     * 查询对象ID和手自动设定恢复时间映射
     *
     * @param objectIds 对象ID
     * @return 对象ID -> 手自动设定恢复时间
     * @since 5.240702.0
     */
    public Map<String, LocalDateTime> queryObjectIdAndManualAutoSetRecoverTime(@NonNull Collection<String> objectIds) {
        List<EquipRecord> list = equipRecordService.getEquipRecordByInfoCodeDescList(
                ContextUtil.getGroupCode(), ContextUtil.getProjectId(),
                new ArrayList<>(objectIds), InfoCodeEnum.MANUAL_AUTO_SET.getCode(), -1);

        // 按照 ID 分组
        Map<String, EquipRecord> objectIdAndEquipRecord = list.stream().collect((Collectors.toMap(EquipRecord::getObjId, Function.identity(), (o1, o2) -> o2)));

        Map<String, LocalDateTime> result = new HashMap<>();
        if (MapUtils.isNotEmpty(objectIdAndEquipRecord)) {
            for (String objectId : objectIds) {
                EquipRecord equipRecord = objectIdAndEquipRecord.get(objectId);
                if (Objects.nonNull(equipRecord) && Objects.nonNull(equipRecord.getChangeContent())) {
                    LocalDateTime expireTime = equipRecord.getChangeContent().toExecTime();
                    if (Objects.nonNull(expireTime)) {
                        result.put(objectId, expireTime);
                    }
                }
            }
        }
        return result;
    }

    /**
     * 查询对象IOT设定信息点恢复时间
     *
     * @param objectIds 对象ID
     * @param infoCodes 信息点列表, 为空时查询全部
     * @return 对象ID -> [信息点编码 -> 恢复时间]
     * @since 5.240702.0
     */
    public Map<String, Map<String, LocalDateTime>> queryObjectIdIotSettingAndRecoverTime(
            @NonNull Collection<String> objectIds, @Nullable Set<String> infoCodes) {
        List<EquipRecord> equipRecords = equipRecordService.queryEquipRecordBySourceFrom(
                ContextUtil.getGroupCode(), ContextUtil.getProjectId(), new HashSet<>(objectIds),
                infoCodes, SourceFromEnum.RELEASE_INTERVENTION);
        Map<String, Map<String, LocalDateTime>> result = new HashMap<>();
        if (CollectionUtils.isNotEmpty(equipRecords)) {
            Map<String, List<EquipRecord>> objectIdAndAllEquipRecord = equipRecords.stream()
                    .collect(Collectors.groupingBy(EquipRecord::getObjId));
            for (Map.Entry<String, List<EquipRecord>> entry : objectIdAndAllEquipRecord.entrySet()) {
                String objectId = entry.getKey();
                List<EquipRecord> records = entry.getValue();
                for (EquipRecord equipRecord : records) {
                    String infoCode = equipRecord.getInfoCode();
                    LocalDateTime expireTime = equipRecord.getChangeContent().toExecTime();
                    if (Objects.nonNull(expireTime)) {
                        MapExecutor.dynamicPut(result, objectId, infoCode, expireTime, HashMap::new);
                    }
                }
            }
        }
        return result;
    }
}




