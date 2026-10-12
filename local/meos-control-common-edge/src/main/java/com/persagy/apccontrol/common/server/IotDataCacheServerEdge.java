package com.persagy.apccontrol.common.server;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.model.dto.QueryTwinDataDTO;
import com.persagy.apccontrol.basic.model.dto.QueryTwinDataOnMomentDTO;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFractionalPrecisionRuleDTO;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdPersistenceTimeTypeEnum;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.outside.dto.DtpPointInfo;
import com.persagy.apccontrol.basic.outside.feign.model.dto.DtpTwinIotData;
import com.persagy.apccontrol.basic.outside.feign.model.dto.DtpTwinR;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdTwinObjectRuleByIdQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.rwd.DtpRwdTwinObjectRuleByIdResponse;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.structure.iot.IotConverter;
import com.persagy.apccontrol.basic.utils.IotDataUtil;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.apccontrol.common.outside.feign.DtpTwinClient;
import com.persagy.apccontrol.common.service.IotDataCacheService;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 边缘端IOT数据
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/4/12 15:44
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class IotDataCacheServerEdge extends AbstractIotDataCacheServer<FeedbackInfoMsgMqttVO> {

    private final IotConverter iotConverter;
    private final DtpRwdClientService dtpRwdClientService;
    private final DtpKnowledgeClientService dtpKnowledgeClientService;
    private final IotDataCacheService iotDataCacheService;
    private final DtpTwinClient dtpTwinClient;

    @Override
    public Map<String, Set<String>> mapObjectIdAndExistsRuleInfoCodes(
            @NonNull String projectId,
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

    @Override
    public List<InfoCodeValueVO> listInfoCodeValue(
            @NonNull String projectId, @Nullable DtpRwdClassCodeEnum classCode,
            @NonNull Collection<String> objectIds, @NonNull Collection<String> infoCodes) {
        if (CollectionUtils.isEmpty(objectIds) || CollectionUtils.isEmpty(infoCodes)) {
            return Lists.newArrayList();
        }
        List<FeedbackInfoMsgMqttVO> feedbackInfos = iotDataCacheService.queryExtendIotRealDataList(Lists.newArrayList(objectIds), Lists.newArrayList(infoCodes));
        if (CollectionUtils.isEmpty(feedbackInfos)) {
            return Lists.newArrayList();
        }

        Map<String, DtpKnowledgeInfoCodeDTO> infoCodeAndData = Collections.emptyMap();
        Map<String, Map<String, DtpKnowledgeInfoCodeDTO>> infoCodeAndDataClassCode = new HashMap<>(16);
        if (Objects.nonNull(classCode)) {
            infoCodeAndData = dtpKnowledgeClientService.mapInfoCodeAndData(true, false, classCode.value());
        } else {
            // 拼接datasource
            Set<String> classCodeSet = new HashSet<>();
            for (FeedbackInfoMsgMqttVO dtpPointInfo : feedbackInfos) {
                classCodeSet.add(dtpPointInfo.getClassCode());
            }
            for (String classCodeNew : classCodeSet) {
                Map<String, DtpKnowledgeInfoCodeDTO> infoCodeAndDataNew = dtpKnowledgeClientService.mapInfoCodeAndData(true, false, classCodeNew);
                infoCodeAndDataClassCode.put(classCodeNew, infoCodeAndDataNew);
            }
        }

        List<InfoCodeValueVO> result = new ArrayList<>();
        for (FeedbackInfoMsgMqttVO feedbackInfo : feedbackInfos) {
            String infoCode = feedbackInfo.getInfoCode();

            DtpKnowledgeInfoCodeDTO infoCodeDefine = infoCodeAndData.get(infoCode);
            if (Objects.isNull(infoCodeDefine)) {
                Map<String, DtpKnowledgeInfoCodeDTO> dtpKnowledgeInfoCodeDTOMap = infoCodeAndDataClassCode.get(feedbackInfo.getClassCode());
                if (dtpKnowledgeInfoCodeDTOMap != null) {
                    infoCodeDefine = dtpKnowledgeInfoCodeDTOMap.get(infoCode);
                }
            }
            result.add(iotConverter.toInfoCodeValueVo(feedbackInfo, infoCodeDefine));
        }
        return result;
    }

    @Override
    public List<InfoCodeValueVO> listInfoCodeValueInOneHour(
            @NonNull Collection<String> objectIds, @NonNull Collection<String> infoCodes) {
        if (CollectionUtils.isEmpty(objectIds) || CollectionUtils.isEmpty(infoCodes)) {
            return Lists.newArrayList();
        }
        List<FeedbackInfoMsgMqttVO> feedbackInfos = iotDataCacheService.queryIotRealDataList(
                Lists.newArrayList(objectIds), Lists.newArrayList(infoCodes));
        if (CollectionUtils.isEmpty(feedbackInfos)) {
            return Lists.newArrayList();
        }

        List<InfoCodeValueVO> result = new ArrayList<>();
        for (FeedbackInfoMsgMqttVO feedbackInfo : feedbackInfos) {
            result.add(iotConverter.toInfoCodeValueVo(feedbackInfo, null));
        }
        return result;
    }

    @Override
    public List<DtpRwdFractionalPrecisionRuleDTO> queryInfoPointPeroid(
            @NonNull Set<String> objectIdList, @Nullable Set<String> infoCodeList) {
        return Lists.newArrayList();
    }

    public void setStringCacheData(String redisKey, String value) {
        this.iotDataCacheService.setStringCacheData(redisKey, value);
    }

    public String getStringCacheData(String redisKey) {
        return this.iotDataCacheService.getStringCacheData(redisKey);
    }

    public void setDataValueAndTime(FeedbackInfoMsgMqttVO infoMsgMqttVO, RcInfoCodeCfg rcInfoCodeCfg, JSONObject objectInfo) {
        this.iotDataCacheService.setDataValueAndTime(infoMsgMqttVO, rcInfoCodeCfg, objectInfo);
    }

    @Override
    public String queryIotRealData(String groupCode, String projectId, String objectId, RcInfoCodeCfg infoCodeCfg) {
        return infoCodeCfg == null ? null : this.iotDataCacheService.queryIotRealData(objectId, infoCodeCfg.getInfoCode(), infoCodeCfg);
    }

    @Override
    public String queryIotRealData(String groupCode, String projectId, String objectId, String classCode, String infoCode) {
        RcInfoCodeCfg infoCodeConfig = this.localRedisCacheHandler.getInfoCodeConfig(classCode, infoCode);
        return this.iotDataCacheService.queryIotRealData(objectId, infoCode, infoCodeConfig);
    }

    @Override
    public FeedbackInfoMsgMqttVO queryIotRealDataVO(String groupCode, String projectId, String objectId, RcInfoCodeCfg infoCodeCfg) {
        return this.iotDataCacheService.queryIotRealDataVO(objectId, infoCodeCfg.getInfoCode(), infoCodeCfg);
    }

    @Override
    public FeedbackInfoMsgMqttVO queryIotRealDataVO(String groupCode, String projectId, String objectId, String classCode, String infoCode) {
        RcInfoCodeCfg infoCodeConfig = this.localRedisCacheHandler.getInfoCodeConfig(classCode, infoCode);
        return this.iotDataCacheService.queryIotRealDataVO(objectId, infoCode, infoCodeConfig);
    }

    @Override
    public FeedbackInfoMsgMqttVO queryIotCtrlDataVO(String groupCode, String projectId, String objectId, String classCode, String infoCode) {
        return this.iotDataCacheService.queryIotCtrlDataVO(objectId, infoCode, false);
    }

    @Override
    public FeedbackInfoMsgMqttVO queryIotRealDataRedisVO(String objectId, String infoCode) {
        return this.iotDataCacheService.queryIotRealDataRedisVO(objectId, infoCode);
    }

    @Override
    public List<FeedbackInfoMsgMqttVO> queryIotRealDataList(String groupCode, String projectId, String objectId, List<RcInfoCodeCfg> infoCodeCfgs) {
        List<FeedbackInfoMsgMqttVO> result = new ArrayList<>();
        if (CollectionUtil.isEmpty(infoCodeCfgs)) {
            return result;
        }
        Map<String, RcInfoCodeCfg> infoCodeCfgMap = infoCodeCfgs.stream().collect(Collectors.toMap(RcInfoCodeCfg::getInfoCode, Function.identity(), (v1, v2) -> v2));
        List<FeedbackInfoMsgMqttVO> infoMsgMqttVOS = this.queryIotRealDataList(groupCode, projectId, objectId, infoCodeCfgs.get(0).getClassCode(), Lists.newArrayList(infoCodeCfgMap.keySet()));
        for (FeedbackInfoMsgMqttVO infoMsgMqttVO : infoMsgMqttVOS) {
            RcInfoCodeCfg rcInfoCodeCfg = infoCodeCfgMap.get(infoMsgMqttVO.getInfoCode());
            IotDataUtil.overrideDataValue(infoMsgMqttVO, rcInfoCodeCfg);
        }
        return infoMsgMqttVOS;
    }

    @Override
    public Map<String, Map<String, String>> queryIotRealDataMap(String groupCode, String projectId, Set<String> objectIds, Set<String> infoCodes) {
        List<FeedbackInfoMsgMqttVO> feedbackInfoMsgMqttVOS = this.iotDataCacheService.queryIotRealDataList(objectIds, Lists.newArrayList(infoCodes));
        return feedbackInfoMsgMqttVOS.stream().collect(Collectors.groupingBy(FeedbackInfoMsgMqttVO::getObjectId, Collectors.toMap(FeedbackInfoMsgMqttVO::getInfoCode, FeedbackInfoMsgMqttVO::getDataValue)));
    }

    @Override
    public List<DtpPointInfo> queryCommonIotRealDataList(String groupCode, String projectId, String objectId, List<RcInfoCodeCfg> infoCodeCfgs) {
        List<DtpPointInfo> result = new ArrayList<>();
        if (CollectionUtil.isEmpty(infoCodeCfgs)) {
            return result;
        }
        Map<String, RcInfoCodeCfg> infoCodeCfgMap = infoCodeCfgs.stream().collect(Collectors.toMap(RcInfoCodeCfg::getInfoCode, Function.identity(), (v1, v2) -> v2));
        List<FeedbackInfoMsgMqttVO> feedbackInfoMsgMqttVOS = this.queryIotRealDataList(groupCode, projectId, objectId, infoCodeCfgs.get(0).getClassCode(), Lists.newArrayList(infoCodeCfgMap.keySet()));
        for (FeedbackInfoMsgMqttVO infoMsgMqttVO : feedbackInfoMsgMqttVOS) {
            RcInfoCodeCfg rcInfoCodeCfg = infoCodeCfgMap.get(infoMsgMqttVO.getInfoCode());
            IotDataUtil.overrideDataValue(infoMsgMqttVO, rcInfoCodeCfg);
            result.add(new DtpPointInfo(infoMsgMqttVO.getObjectId(), infoMsgMqttVO.getInfoCode(),
                    infoMsgMqttVO.getDataValue(), infoMsgMqttVO.getDataTime(), infoMsgMqttVO.getDataIndate()));
        }
        return result;
    }

    @Override
    public List<FeedbackInfoMsgMqttVO> queryIotRealDataList(String groupCode, String projectId, String objectId, String classCode, List<String> infoCodes) {
        List<FeedbackInfoMsgMqttVO> result = new ArrayList<>();
        if (CollectionUtil.isEmpty(infoCodes)) {
            return result;
        }
        return this.iotDataCacheService.queryIotRealDataVO(objectId, infoCodes);
    }

    /**
     * @param groupCode
     * @param projectId
     * @param objectIdList
     * @param infoCodeList
     * @return
     */
    @Override
    public List<FeedbackInfoMsgMqttVO> queryIotRealDataListByList(
            String groupCode, String projectId,
            Collection<String> objectIdList, Collection<String> infoCodeList) {
        if (CollectionUtils.isEmpty(objectIdList) || CollectionUtils.isEmpty(infoCodeList)) {
            return Lists.newArrayList();
        }
        return this.iotDataCacheService.queryExtendIotRealDataList(Lists.newArrayList(objectIdList), Lists.newArrayList(infoCodeList));
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
                                                                                      @NotNull Collection<String> objectIds,
                                                                                      @NotNull Collection<String> infoCodeList) {
        List<QueryTwinDataOnMomentDTO> objectList = new ArrayList<>();
        Set<String> infoCodes = new HashSet<>(infoCodeList);
        for (String objectId : objectIds) {
            QueryTwinDataOnMomentDTO queryTwinDataOnMoment = new QueryTwinDataOnMomentDTO();
            queryTwinDataOnMoment.setObjectId(objectId);
            queryTwinDataOnMoment.setInfoCodes(infoCodes);
            objectList.add(queryTwinDataOnMoment);
        }

        QueryTwinDataDTO queryTwinData = new QueryTwinDataDTO();

        queryTwinData.setObjects(objectList);

        // 从中台查询iot数据
        DtpTwinR<DtpTwinIotData> dtpTwinIotDataDtpTwinR =
                dtpTwinClient.queryTwinDataHistory(ContextUtil.getGroupCode(), ContextUtil.getProjectId(), null, queryTwinData);
        if (dtpTwinIotDataDtpTwinR.getContent().size() > 0) {
            List<DtpTwinIotData> contentList = dtpTwinIotDataDtpTwinR.getContent();
            contentList.forEach(x -> x.setDataTime(DateUtils.getNearFiveMinutes(x.getDataTime())));
            // 按空间分组，再按信息点，再按时间，最后的value是数值方便取值
            return contentList.stream()
                    .collect(Collectors.groupingBy(
                            DtpTwinIotData::getObjectId,
                            Collectors.groupingBy(
                                    DtpTwinIotData::getInfoCode,
                                    Collectors.toMap(
                                            DtpTwinIotData::getDataTime,
                                            DtpTwinIotData::getDataValue,
                                            (existing, replacement) -> replacement
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
