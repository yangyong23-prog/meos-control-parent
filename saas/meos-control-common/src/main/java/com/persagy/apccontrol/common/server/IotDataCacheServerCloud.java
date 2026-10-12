package com.persagy.apccontrol.common.server;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.model.dto.EnergyDataQueryParam;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFractionalPrecisionRuleDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdPersistenceTimeTypeEnum;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.outside.dto.DtpPointInfo;
import com.persagy.apccontrol.basic.outside.dto.EnergyDataResponse;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.ObjectInstantiateQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.BizDtpRwdService;
import com.persagy.apccontrol.basic.structure.iot.IotConverter;
import com.persagy.apccontrol.basic.utils.IotDataUtil;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.enums.ResponseCode;
import com.persagy.framework.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 类功能描述
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/4/12 15:46
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class IotDataCacheServerCloud extends AbstractIotDataCacheServer<DtpPointInfo> {

    private final IotConverter iotConverter;
    private final DtpRwdClientService dtpRwdClientService;
    private final BizDtpRwdService bizDtpRwdService;
    private final DtpKnowledgeClientService dtpKnowledgeClientService;
    @Lazy
    @Autowired
    private AbstractCommonServer abstractCommonServer;

    @Override
    public Map<String, Set<String>> mapObjectIdAndExistsRuleInfoCodes(
            @NonNull String projectId,
            @NonNull Map<String, Set<String>> classCodeAndObjectIds,
            @NonNull Collection<String> infoCodes) {
        List<ObjectInstantiateQueryParam> queryParams = new ArrayList<>();
        for (Set<String> objectIds : classCodeAndObjectIds.values()) {
            for (String objectId : objectIds) {
                for (String infoCode : infoCodes) {
                    queryParams.add(ObjectInstantiateQueryParam.batchInstance(objectId, infoCode));
                }
            }
        }
        return dtpRwdClientService.mapObjectIdAndInstantiateInfoCodes(projectId, queryParams);
    }

    @Override
    public List<InfoCodeValueVO> listInfoCodeValue(
            @NonNull String projectId, @Nullable DtpRwdClassCodeEnum classCode,
            @NonNull Collection<String> objectIds, @NonNull Collection<String> infoCodes) {
        if (CollectionUtils.isEmpty(objectIds) || CollectionUtils.isEmpty(infoCodes)) {
            return Lists.newArrayList();
        }
        List<DtpPointInfo> dtpPointInfos = queryIotRealDataListByList(
                ContextUtil.getGroupCode(), projectId, objectIds, infoCodes);
        if (CollectionUtils.isEmpty(dtpPointInfos)) {
            return Lists.newArrayList();
        }

        Map<String, DtpKnowledgeInfoCodeDTO> infoCodeAndData = Collections.emptyMap();

        Map<String, Map<String, DtpKnowledgeInfoCodeDTO>> infoCodeAndDataClassCode = new HashMap<>(16);
        if (Objects.nonNull(classCode)) {
            infoCodeAndData = dtpKnowledgeClientService.mapInfoCodeAndData(true, false, classCode.value());
        } else {
            //拼接datasource
            Set<String> classCodeSet = new HashSet<>();
            for (DtpPointInfo dtpPointInfo : dtpPointInfos) {
                classCodeSet.add(dtpPointInfo.getClassCode());
            }
            for (String classCodeNew : classCodeSet) {
                Map<String, DtpKnowledgeInfoCodeDTO> infoCodeAndDataNew = dtpKnowledgeClientService.mapInfoCodeAndData(true, false, classCodeNew);
                infoCodeAndDataClassCode.put(classCodeNew, infoCodeAndDataNew);
            }
        }

        List<InfoCodeValueVO> result = new ArrayList<>();
        for (DtpPointInfo dtpPointInfo : dtpPointInfos) {
            String infoCode = dtpPointInfo.getInfoCode();

            DtpKnowledgeInfoCodeDTO infoCodeDefine = infoCodeAndData.get(infoCode);
            if (Objects.isNull(infoCodeDefine)) {
                Map<String, DtpKnowledgeInfoCodeDTO> dtpKnowledgeInfoCodeDTOMap = infoCodeAndDataClassCode.get(dtpPointInfo.getClassCode());
                if (dtpKnowledgeInfoCodeDTOMap != null) {
                    infoCodeDefine = dtpKnowledgeInfoCodeDTOMap.get(infoCode);
                }
            }
            result.add(iotConverter.toInfoCodeValueVo(dtpPointInfo, infoCodeDefine));
        }
        return result;
    }

    /**
     * 获取对象id的对象类编码
     *
     * @param objectIds 对象id
     * @return 对象id和对象类编码的map
     */
    private Map<String, String> getObjectClassCode(String projectId, Collection<String> objectIds, DtpRwdClassCodeEnum classCode) {
        if (Objects.nonNull(classCode)) {
            Map<String, String> idAndClassCodeMap = new HashMap<>();
            objectIds.forEach(oneObjectId -> {
                if (!idAndClassCodeMap.containsKey(oneObjectId)) {
                    idAndClassCodeMap.put(oneObjectId, classCode.value());
                }
            });
            return idAndClassCodeMap;
        } else {
            DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
            param.setId(objectIds);
            param.addWithColumn(DtpRwdObjectDTO.Fields.classCode, DtpRwdObjectDTO.Fields.id);
            List<DtpRwdObjectDTO> dtpRwdObjects = dtpRwdClientService.listObject(false, false, projectId, param);
            return dtpRwdObjects.stream()
                    .collect(Collectors.toMap(DtpRwdObjectDTO::getId, DtpRwdObjectDTO::getClassCode));
        }
    }


    @Override
    public List<InfoCodeValueVO> listInfoCodeValueInOneHour(
            @NonNull Collection<String> objectIdList, @NonNull Collection<String> infoCodeList) {
        if (CollectionUtil.isEmpty(objectIdList) || CollectionUtils.isEmpty(infoCodeList)) {
            return Lists.newArrayList();
        }

        // 获取最后的IOT数据
        List<EnergyDataResponse> responses = this.bizDtpRwdService.queryIpDataOnMomentInOneHour(Sets.newHashSet(objectIdList), Sets.newHashSet(infoCodeList));
        if (CollectionUtil.isEmpty(responses)) {
            return new ArrayList<>();
        }

        // 数据处理
        List<DtpPointInfo> pointInfos = new ArrayList<>();
        for (EnergyDataResponse energyDataResponse : responses) {
            DtpPointInfo dtpPointInfo = this.getDtpPointInfo(energyDataResponse, null);
            if (dtpPointInfo != null) {
                pointInfos.add(dtpPointInfo);
            }
        }

        List<InfoCodeValueVO> result = new ArrayList<>();
        for (DtpPointInfo dtpPointInfo : pointInfos) {
            result.add(iotConverter.toInfoCodeValueVo(dtpPointInfo, null));
        }
        return result;
    }

    @Override
    public List<DtpRwdFractionalPrecisionRuleDTO> queryInfoPointPeroid(
            @NonNull Set<String> objectIdList, @Nullable Set<String> infoCodeList) {
        if (CollectionUtil.isEmpty(objectIdList)) {
            return Lists.newArrayList();
        }
        // 查询精度
        List<DtpRwdFractionalPrecisionRuleDTO> responses = this.dtpRwdClientService.queryInfoPointPeroid(objectIdList);
        if (CollectionUtil.isEmpty(responses)) {
            return new ArrayList<>();
        }
        if (CollectionUtils.isEmpty(infoCodeList)) {
            return responses;
        }
        return responses.stream()
                .filter(ruleDTO -> infoCodeList.contains(ruleDTO.getInfoCode()))
                .collect(Collectors.toList());
    }

    @Override
    public String queryIotRealData(String groupCode, String projectId, String objectId, RcInfoCodeCfg infoCodeCfg) {
        DtpPointInfo pointInfo = this.queryIotRealDataVO(groupCode, projectId, objectId, infoCodeCfg);
        return pointInfo == null ? null : pointInfo.getIpValue();
    }

    @Override
    public String queryIotRealData(String groupCode, String projectId, String objectId, String classCode, String infoCode) {
        DtpPointInfo pointInfo = this.queryIotRealDataVO(groupCode, projectId, objectId, classCode, infoCode);
        return pointInfo == null ? null : pointInfo.getIpValue();
    }

    @Override
    public DtpPointInfo queryIotRealDataVO(String groupCode, String projectId, String objectId, RcInfoCodeCfg infoCodeCfg) {
        List<DtpPointInfo> pointInfos = this.queryIotRealDataList(groupCode, projectId, objectId, Lists.newArrayList(infoCodeCfg));
        return CollectionUtil.isEmpty(pointInfos) ? null : pointInfos.get(0);
    }

    @Override
    public DtpPointInfo queryIotRealDataVO(String groupCode, String projectId, String objectId, String classCode, String infoCode) {
        List<DtpPointInfo> pointInfos = this.queryIotRealDataList(groupCode, projectId, objectId, classCode, StrUtil.isBlank(infoCode) ? null : Lists.newArrayList(infoCode));
        return CollectionUtil.isEmpty(pointInfos) ? null : pointInfos.get(0);
    }

    @Override
    public DtpPointInfo queryIotCtrlDataVO(String groupCode, String projectId, String objectId, String classCode, String infoCode) {
        throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_6917cbd4868145068417bd3b79f65995"));
    }

    @Override
    public DtpPointInfo queryIotRealDataRedisVO(String objectId, String infoCode) {
        // 云端不支持从 Redis 缓存中查询开启中、关闭中信息点
        return null;
    }

    @Override
    public List<DtpPointInfo> queryIotRealDataList(String groupCode, String projectId, String objectId, List<RcInfoCodeCfg> infoCodeCfgs) {
        List<DtpPointInfo> pointInfos = new ArrayList<>();
        if (CollectionUtil.isEmpty(infoCodeCfgs)) {
            return pointInfos;
        }

        try {
            // 反馈默认一小时有效期，设定值取最新
            Map<String, RcInfoCodeCfg> infoCodeCfgMap = infoCodeCfgs.stream().filter(item -> item.isDynamicParameter())
                    .collect(Collectors.toMap(RcInfoCodeCfg::getInfoCode, Function.identity(), (v1, v2) -> v2));
            List<EnergyDataResponse> responses = this.bizDtpRwdService.queryIpDataOnMoment(groupCode, projectId, Lists.newArrayList(objectId), Lists.newArrayList(infoCodeCfgMap.keySet()));
            if (CollectionUtil.isEmpty(responses)) {
                return null;
            }

            // 数据处理
            for (EnergyDataResponse energyDataResponse : responses) {
                RcInfoCodeCfg infoCodeCfg = infoCodeCfgMap.get(energyDataResponse.getInfoCode());

                DtpPointInfo dtpPointInfo = this.getDtpPointInfo(energyDataResponse, infoCodeCfg);
                if (dtpPointInfo != null) {
                    pointInfos.add(dtpPointInfo);
                }
            }
        } catch (Exception e) {
            log.info("云端-IOT数据查询异常", e);
        }
        return pointInfos;
    }

    @Override
    public Map<String, Map<String, String>> queryIotRealDataMap(String groupCode, String projectId, Set<String> objectId, Set<String> infoCodes) {
        Map<String, Map<String, String>> resultMap = new HashMap<>();

        List<EnergyDataResponse> energyDataResponses = this.bizDtpRwdService.queryIpDataOnMoment(groupCode, projectId, Lists.newArrayList(objectId), Lists.newArrayList(infoCodes));
        if (CollectionUtil.isEmpty(energyDataResponses)) {
            return resultMap;
        }

        // 有效性判断
        for (EnergyDataResponse energyDataRespons : energyDataResponses) {
            DtpPointInfo dtpPointInfo = this.getDtpPointInfo(energyDataRespons, null);
            if (dtpPointInfo == null) {
                continue;
            }
            if (!resultMap.containsKey(dtpPointInfo.getObjectId())) {
                resultMap.put(dtpPointInfo.getObjectId(), new HashMap<>());
            }
            resultMap.get(dtpPointInfo.getObjectId()).computeIfAbsent(dtpPointInfo.getInfoCode(), e -> dtpPointInfo.getDataValue());
        }

        return resultMap;
    }

    @Override
    public List<DtpPointInfo> queryCommonIotRealDataList(String groupCode, String projectId, String objectId, List<RcInfoCodeCfg> infoCodeCfgs) {
        return this.queryIotRealDataList(groupCode, projectId, objectId, infoCodeCfgs);
    }

    @Override
    public List<DtpPointInfo> queryIotRealDataList(String groupCode, String projectId, String objectId, String classCode, List<String> infoCodes) {
        List<DtpPointInfo> pointInfos = new ArrayList<>();
        if (CollectionUtil.isEmpty(infoCodes)) {
            return pointInfos;
        }
        try {
            List<EnergyDataResponse> responses = this.bizDtpRwdService.queryIpDataOnMoment(groupCode, projectId, Lists.newArrayList(objectId), infoCodes);
            return processDtpPointInfo(classCode, responses, pointInfos);
        } catch (Exception e) {
            log.info("云端-IOT数据查询异常", e);
        }
        return pointInfos;
    }

    /**
     * 处理响应信息点和配置
     *
     * @param classCode  对象类编码
     * @param responses  响应数据
     * @param pointInfos 点位信息
     * @return 响应信息
     */
    private List<DtpPointInfo> processDtpPointInfo(String classCode,
                                                   List<EnergyDataResponse> responses,
                                                   List<DtpPointInfo> pointInfos) {
        if (CollectionUtil.isEmpty(responses)) {
            return pointInfos;
        }

        // 数据处理
        for (EnergyDataResponse energyDataResponse : responses) {
            String infoCode = energyDataResponse.getInfoCode();
            RcInfoCodeCfg infoCodeCfg = this.localRedisCacheHandler.getInfoCodeConfig(classCode, infoCode);

            DtpPointInfo dtpPointInfo = this.getDtpPointInfo(energyDataResponse, infoCodeCfg);
            if (dtpPointInfo != null) {
                pointInfos.add(dtpPointInfo);
            }
        }
        return pointInfos;
    }

    @Override
    public List<DtpPointInfo> queryIotRealDataListByList(
            String groupCode, String projectId,
            Collection<String> objectIdList, Collection<String> infoCodeList) {
        if (CollectionUtil.isEmpty(objectIdList) || CollectionUtils.isEmpty(infoCodeList)) {
            return Lists.newArrayList();
        }
        try {
            Set<String> objectIds = Sets.newHashSet(objectIdList);
            Set<String> infoCodes = Sets.newHashSet(infoCodeList);
            // 1.查询当前中台交付的所有对象
            List<JSONObject> objectList = this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList(objectIds));
            if (CollectionUtil.isEmpty(objectList)) {
                return Lists.newArrayList();
            }
            Map<String, String> objec_class_code_map = objectList.stream().collect(Collectors.toMap(key -> key.getString("id"), value -> value.getString("classCode"), (v1, v2) -> v2));
            // 获取最后的IOT数据
            List<EnergyDataResponse> responses = this.bizDtpRwdService.queryIpDataOnMoment(groupCode, projectId, Lists.newArrayList(objectIds), Lists.newArrayList(infoCodes));
            return processInfoCodeConfigAndValue(objec_class_code_map, infoCodes, responses);
        } catch (Exception e) {
            log.info("云端-IOT数据查询异常", e);
        }
        return Lists.newArrayList();
    }

    private List<DtpPointInfo> processInfoCodeConfigAndValue(Map<String, String> objecClassCodeMap,
                                                             Set<String> infoCodes,
                                                             List<EnergyDataResponse> responses) {
        if (CollectionUtil.isEmpty(responses)) {
            return Lists.newArrayList();
        }
        // 获取所有的信息点配置
        Map<String, Map<String, RcInfoCodeCfg>> class_info_code_map = new HashMap<>();
        Set<String> classCodes = Sets.newHashSet(objecClassCodeMap.values());
        for (String classCode : classCodes) {
            List<RcInfoCodeCfg> infoCodeConfigs = this.localRedisCacheHandler.getInfoCodeConfigs(classCode, infoCodes);
            Map<String, RcInfoCodeCfg> infoCodeCfgMap = CollectionUtil.isEmpty(infoCodeConfigs) ? new HashMap<>()
                    : infoCodeConfigs.stream().collect(Collectors.toMap(RcInfoCodeCfg::getInfoCode, Function.identity(), (v1, v2) -> v2));
            class_info_code_map.put(classCode, infoCodeCfgMap);
        }

        // 数据处理
        List<DtpPointInfo> pointInfos = new ArrayList<>();
        for (EnergyDataResponse energyDataResponse : responses) {
            String infoCode = energyDataResponse.getInfoCode();
            String classCode = objecClassCodeMap.get(energyDataResponse.getObjectId());
            if (StrUtil.isBlank(classCode)) {
                continue;
            }
            RcInfoCodeCfg rcInfoCodeCfg = class_info_code_map.get(classCode) == null ? null : class_info_code_map.get(classCode).get(infoCode);

            DtpPointInfo dtpPointInfo = this.getDtpPointInfo(energyDataResponse, rcInfoCodeCfg);
            if (dtpPointInfo != null) {
                if(dtpPointInfo.getClassCode() == null){
                    dtpPointInfo.setClassCode(classCode);
                }
                pointInfos.add(dtpPointInfo);
            }
        }
        return pointInfos;
    }

    /**
     * 处理信息点数据，反馈默认取最近一小时，控制无限制
     *
     * @param energyDataResponse
     * @param infoCodeCfg
     * @return
     */
    private DtpPointInfo getDtpPointInfo(EnergyDataResponse energyDataResponse, RcInfoCodeCfg infoCodeCfg) {
        if (energyDataResponse == null) {
            return null;
        }

        DtpPointInfo pointInfo = new DtpPointInfo();
        BeanUtils.copyProperties(energyDataResponse, pointInfo, "ipTime", "ipValue");
        LocalDateTime dateTime = LocalDateTime.parse(energyDataResponse.getIpTime(), DateTimeFormatter.ofPattern(DatePattern.PURE_DATETIME_PATTERN));
        pointInfo.setIpTime(dateTime.format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN)));
        pointInfo.setIpValue(energyDataResponse.getIpValue() == null ? null : energyDataResponse.getIpValue().toPlainString());

        // dataValue 重写
        pointInfo.setIpValue(IotDataUtil.overrideDataValue(pointInfo.getInfoCode(), pointInfo.getIpValue(), infoCodeCfg));

        pointInfo.setDataTime(pointInfo.getIpTime());
        pointInfo.setDataValue(pointInfo.getIpValue());

        // 设定值，直接返回
        if (infoCodeCfg != null) {
            pointInfo.setClassCode(infoCodeCfg.getClassCode());
        }
        if (infoCodeCfg != null && infoCodeCfg.isSettingParameter()) {
            return pointInfo;
        }

        // 反馈值，取最近一小时
        LocalDateTime currentTime = DateUtils.now().withSecond(0).withNano(0).plusMinutes(-60);
        return currentTime.isBefore(dateTime) || currentTime.equals(dateTime) ? pointInfo : null;
    }

    /**
     * iot历史数据
     *
     * @param objectIds    对象ID
     * @param infoCodeList 信息点编码
     * @return 对象ID -> [信息点编码 -> 数值]
     * @since 5.240702.0
     */
    @Override
    public Map<String, Map<String, Map<String, String>>> queryObjectIdInfoCodeHistory(@NonNull DtpRwdPersistenceTimeTypeEnum ipType,
                                                                                      @NonNull Collection<String> objectIds,
                                                                                      @NonNull Collection<String> infoCodeList) {
        // 仅支持co2和tdb两个信息点
        EnergyDataQueryParam bodyDto = abstractCommonServer.getRwdInfo(new ArrayList<>(objectIds), new ArrayList<>(infoCodeList));
        // 将时间设为当天的 0 点
        bodyDto.setStartTime(DateUtils.get24TimeStringBeforeHour());
        bodyDto.setEndTime(DateUtils.getNowTimeString());
        bodyDto.setIpType(ipType.value());

        // iot数据查询
        List<EnergyDataResponse> ipDataList = this.bizDtpRwdService.queryIpDataInPeriod(bodyDto);
        if (CollectionUtils.isNotEmpty(ipDataList)) {
            // 按空间分组，再按信息点，再按时间，最后的value是数值方便取值
            return ipDataList.stream()
                    .collect(Collectors.groupingBy(
                            EnergyDataResponse::getObjectId,
                            Collectors.groupingBy(
                                    EnergyDataResponse::getInfoCode,
                                    Collectors.toMap(
                                            EnergyDataResponse::getIpTime,
                                            item -> String.valueOf(item.getIpValue())
                                    )
                            )
                    ));
        }
        return new HashMap<>();

    }

    @Override
    public String logTag() {
        return "IOT数据服务";
    }
}
