package com.persagy.apccontrol.common.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.config.NacosSwitchConfig;
import com.persagy.apccontrol.basic.config.ProjectCfg;
import com.persagy.apccontrol.basic.config.RedisKey;
import com.persagy.apccontrol.basic.handler.LocalRedisCacheHandler;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.QueryTwinDataDTO;
import com.persagy.apccontrol.basic.model.dto.QueryTwinDataOnMomentDTO;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.enumtype.EnumIotDataType;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.service.BizDtpRwdService;
import com.persagy.apccontrol.basic.utils.ControlLogUtil;
import com.persagy.apccontrol.basic.utils.IotDataUtil;
import com.persagy.apccontrol.basic.outside.feign.model.dto.DtpTwinIotData;
import com.persagy.apccontrol.basic.outside.feign.service.DtpTwinClientService;
import com.persagy.apccontrol.common.service.IotDataCacheService;
import com.persagy.framework.redis.cache.RedisCacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author shaohongbo
 * @date 2023/4/4 19:12
 */
@Slf4j
@Service
public class IotDataCacheServiceImpl implements IotDataCacheService {

    @Autowired
    private ProjectCfg projectCfg;

    @Autowired
    private DtpTwinClientService dtpTwinClientService;

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Autowired
    private BizDtpRwdService bizDtpRwdService;
    @Autowired
    private NacosSwitchConfig nacosSwitchConfig;

    @Lazy
    @Autowired
    private LocalRedisCacheHandler localRedisCacheHandler;

    @Override
    public String queryIotRealData(String objectId, String infoCode, RcInfoCodeCfg infoCodeCfg) {
        FeedbackInfoMsgMqttVO iotDataVo = queryIotRealDataVO(objectId, infoCode, infoCodeCfg);
        return iotDataVo == null ? null : iotDataVo.getDataValue();
    }

    @Override
    public List<FeedbackInfoMsgMqttVO> queryIotRealDataList(Collection<String> objectIdList, List<String> infoCodeList) {
        // 查询IOT数据
        List<QueryTwinDataOnMomentDTO> queryTwinDataOnMomentDTOS = new ArrayList<>();
        for (String objectId : objectIdList) {
            queryTwinDataOnMomentDTOS.add(new QueryTwinDataOnMomentDTO(projectCfg.getProjectId(), objectId, Sets.newHashSet(infoCodeList)));
        }

        QueryTwinDataDTO queryTwinDataDTO = new QueryTwinDataDTO(queryTwinDataOnMomentDTOS);
        List<DtpTwinIotData> iotDataList = this.dtpTwinClientService.queryTwinDataOnMoment(projectCfg.getGroupCode(), projectCfg.getProjectId(), null, queryTwinDataDTO);
        return DtpTwinIotData.getVaildIotDatas(projectCfg.getGroupCode(), projectCfg.getProjectId(), iotDataList);
    }

    @Override
    public List<FeedbackInfoMsgMqttVO> queryExtendIotRealDataList(Collection<String> objectIdList, List<String> infoCodeList) {
        List<JSONObject> objectList = this.bizDtpRwdService.queryObjectList(projectCfg.getGroupCode(), projectCfg.getProjectId(), Lists.newArrayList(objectIdList));
        if (CollectionUtil.isEmpty(objectList)) {
            return Lists.newArrayList();
        }
        Map<String, JSONObject> objectMap = objectList.stream().collect(Collectors.toMap(key -> key.getString("id"), Function.identity(), (v1, v2) -> v2));

        // IOT查询参数组装
        Set<String> classCodes = new HashSet<>();
        List<QueryTwinDataOnMomentDTO> queryTwinDataOnMomentDTOS = new ArrayList<>();
        for (String objectId : objectIdList) {
            JSONObject objectInfo = objectMap.get(objectId);
            if (objectInfo != null) {
                classCodes.add(objectInfo.getString("classCode"));
            }
            queryTwinDataOnMomentDTOS.add(new QueryTwinDataOnMomentDTO(projectCfg.getProjectId(), objectId, Sets.newHashSet(infoCodeList)));
        }

        // 需要查询一次信息点配置
        Map<String, Map<String, RcInfoCodeCfg>> class_info_config_map = new HashMap<>();
        for (String classCode : classCodes) {
            List<RcInfoCodeCfg> infoCodeConfigs = this.localRedisCacheHandler.getInfoCodeConfigs(classCode);
            Map<String, RcInfoCodeCfg> infoCodeCfgMap = CollectionUtil.isEmpty(infoCodeConfigs) ? new HashMap<>()
                    : infoCodeConfigs.stream().collect(Collectors.toMap(RcInfoCodeCfg::getInfoCode, Function.identity(), (v1, v2) -> v2));
            class_info_config_map.put(classCode, infoCodeCfgMap);
        }

        // 查询IOT数据，并对数据进行二次处理
        QueryTwinDataDTO queryTwinDataDTO = new QueryTwinDataDTO(queryTwinDataOnMomentDTOS);
        List<DtpTwinIotData> iotDataList = this.dtpTwinClientService.queryTwinDataOnMoment(projectCfg.getGroupCode(), projectCfg.getProjectId(), null, queryTwinDataDTO);
        List<FeedbackInfoMsgMqttVO> vaildIotDatas = DtpTwinIotData.getVaildIotDatas(projectCfg.getGroupCode(), projectCfg.getProjectId(), iotDataList);
        for (FeedbackInfoMsgMqttVO vaildIotData : vaildIotDatas) {
            JSONObject objectInfo = objectMap.get(vaildIotData.getObjectId());
            vaildIotData.setLocalId(objectInfo == null ? null : objectInfo.getString("localId"));
            vaildIotData.setLocalName(objectInfo == null ? null : objectInfo.getString("localName"));
            RcInfoCodeCfg rcInfoCodeCfg = class_info_config_map.get(vaildIotData.getClassCode()) == null ? null : class_info_config_map.get(vaildIotData.getClassCode()).get(vaildIotData.getInfoCode());
            IotDataUtil.overrideDataValue(vaildIotData, rcInfoCodeCfg);
        }
        return vaildIotDatas;
    }

    @Override
    public List<FeedbackInfoMsgMqttVO> queryIotRealDataListFromCache(Collection<String> objectIdList, Collection<String> infoCodeList) {
        List<String> keyList = new ArrayList<>();
        objectIdList.forEach(objectId ->
                infoCodeList.forEach(infoCode ->
                        keyList.add(RedisKey.buildIotRealKey(objectId, infoCode))
                )
        );

        List<Object> objects = redisCacheUtil.mGet(keyList);
        List<FeedbackInfoMsgMqttVO> iotDataList = new ArrayList<>();
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(objects)) {
            objects.forEach(o -> {
                if (Objects.nonNull(o) && StringUtils.isNotBlank(String.valueOf(o))) {
                    iotDataList.add(cn.hutool.core.bean.BeanUtil.toBean(o, FeedbackInfoMsgMqttVO.class));
                }
            });
        }

        // 数组为空的返回空数组
        return CollectionUtils.isEmpty(iotDataList)
                ? new ArrayList<>()
                // 过滤设备状态为空的返回空数组
                :
                (
                        iotDataList.stream().noneMatch(x -> Objects.nonNull(x.getDataStatus()))
                                ? new ArrayList<>()
                                // 过滤设备状态不为1的返回空数组
                                :
                                (
                                        iotDataList.stream().anyMatch(x -> x.getDataStatus() == 1)
                                                ? iotDataList.stream().filter(x -> x.getDataStatus() == 1 && Boolean.parseBoolean(x.getTimeRange())).collect(Collectors.toList())
                                                : new ArrayList<>()
                                )
                );
    }

    @Override
    public String queryIotRealDataForFiveMin(String objectId, String infoCode, RcInfoCodeCfg infoCodeCfg) {
        FeedbackInfoMsgMqttVO iotDataVo = queryIotRealDataVO(objectId, infoCode, infoCodeCfg);
        if (ObjectUtil.isEmpty(iotDataVo)) {
            return null;
        }
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DatePattern.PURE_DATETIME_PATTERN);
        LocalDateTime otherTime = LocalDateTime.parse(iotDataVo.getDataTime(), formatter);
        // 判断时间是否在五分钟内
        boolean withinFiveMinutes = currentTime.minus(5, ChronoUnit.MINUTES).isBefore(otherTime)
                && currentTime.plus(5, ChronoUnit.MINUTES).isAfter(otherTime);
        if (withinFiveMinutes) {
            return iotDataVo == null ? null : iotDataVo.getDataStatus() == null ? null : iotDataVo.getDataStatus() == 1 ? iotDataVo.getDataValue() : null;
        } else {
            return null;
        }
    }

    @Override
    public String queryIotCtrlData(String objectId, String infoCode) {
        FeedbackInfoMsgMqttVO iotDataVo = queryIotCtrlDataVO(objectId, infoCode, true);
        return iotDataVo == null ? null : iotDataVo.getDataValue();
    }

    @Override
    public void setStringCacheData(String redisKey, String value) {
        this.redisCacheUtil.set(redisKey, value);
    }

    @Override
    public String getStringCacheData(String redisKey) {
        return this.redisCacheUtil.get(redisKey);
    }

    @Override
    public void insert(FeedbackInfoMsgMqttVO source, RcInfoCodeCfg infoCodeCfg) {
        Integer type = source.getType();
        if (EnumIotDataType.CTRL.getCode().equals(type)) {
            //控制值，或时间常量上不允许过期的，均持久化存储
            redisCacheUtil.set(RedisKey.buildIotCtrlKey(source.getObjectId(), source.getInfoCode()), source);
        } else if (InfoCodeEnum.RC_RUN_STATUS.getCode().equals(source.getInfoCode())) {
            //添加数据时间 和过期时间
            source.setDataTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern(DatePattern.PURE_DATETIME_PATTERN)));
            //冷冻泵有30分钟的延时关 设置 40 分钟过期时间
            int dataIndate = nacosSwitchConfig.getOpenTimeOfDuration();
            source.setDataIndate(dataIndate);
            redisCacheUtil.setEx(RedisKey.buildIotRealKey(source.getObjectId(), source.getInfoCode()), source,
                    dataIndate * 60L);
            ControlLogUtil.info("开启中、关闭中信息点结算结果：{}", source);
        }
    }

    @Override
    public FeedbackInfoMsgMqttVO queryIotRealDataVO(String objectId, String infoCode, RcInfoCodeCfg infoCodeCfg) {
        QueryTwinDataOnMomentDTO queryTwinDataOnMomentDTO = new QueryTwinDataOnMomentDTO(projectCfg.getProjectId(), objectId, Sets.newHashSet(infoCode));
        QueryTwinDataDTO queryTwinDataDTO = new QueryTwinDataDTO(Lists.newArrayList(queryTwinDataOnMomentDTO));
        List<DtpTwinIotData> iotDataList = this.dtpTwinClientService.queryTwinDataOnMoment(projectCfg.getGroupCode(), projectCfg.getProjectId(), null, queryTwinDataDTO);
        List<FeedbackInfoMsgMqttVO> vaildIotDatas = DtpTwinIotData.getVaildIotDatas(projectCfg.getGroupCode(), projectCfg.getProjectId(), iotDataList);
        FeedbackInfoMsgMqttVO infoMsgMqttVO = vaildIotDatas.size() == 0 ? null : vaildIotDatas.get(0);
        IotDataUtil.overrideDataValue(infoMsgMqttVO, infoCodeCfg);
        return infoMsgMqttVO;
    }

    @Override
    public FeedbackInfoMsgMqttVO queryIotRealDataRedisVO(String objectId, String infoCode) {
        Object o = redisCacheUtil.get(RedisKey.buildIotRealKey(objectId, infoCode));
        return o == null ? null : cn.hutool.core.bean.BeanUtil.toBean(o, FeedbackInfoMsgMqttVO.class);
    }

    @Override
    public List<FeedbackInfoMsgMqttVO> queryIotRealDataVO(String objectId, List<String> infoCodes) {
        QueryTwinDataOnMomentDTO queryTwinDataOnMomentDTO = new QueryTwinDataOnMomentDTO(projectCfg.getProjectId(), objectId, Sets.newHashSet(infoCodes));
        QueryTwinDataDTO queryTwinDataDTO = new QueryTwinDataDTO(Lists.newArrayList(queryTwinDataOnMomentDTO));
        List<DtpTwinIotData> iotDataList = this.dtpTwinClientService.queryTwinDataOnMoment(projectCfg.getGroupCode(), projectCfg.getProjectId(), null, queryTwinDataDTO);
        return DtpTwinIotData.getVaildIotDatas(projectCfg.getGroupCode(), projectCfg.getProjectId(), iotDataList);
    }

    @Override
    public FeedbackInfoMsgMqttVO queryIotDataVOByDataType(String objectId, String infoCode, RcInfoCodeCfg infoCodeCfg, EnumIotDataType iotDataType) {
        if (iotDataType.getCode().intValue() == EnumIotDataType.CTRL.value().intValue()) {
            //控制值
            return queryIotCtrlDataVO(objectId, infoCode, false);
        } else {
            //反馈值
            return queryIotRealDataVO(objectId, infoCode, infoCodeCfg);
        }
    }

    @Override
    public FeedbackInfoMsgMqttVO queryIotCtrlDataVO(String objectId, String infoCode, boolean success) {
        Object o = redisCacheUtil.get(RedisKey.buildIotCtrlKey(objectId, infoCode));
        FeedbackInfoMsgMqttVO iotDataVo = o == null ? null : BeanUtil.toBean(o, FeedbackInfoMsgMqttVO.class);
        return !success ? iotDataVo : iotDataVo == null ? null : iotDataVo.getDataStatus() == null ? null : iotDataVo.getDataStatus() == 2 ? iotDataVo : null;
    }

    /**
     * 写入IOT数据及时间
     *
     * @param infoMsgMqttVO
     * @param rcInfoCodeCfg
     * @param objectInfo
     */
    @Override
    public void setDataValueAndTime(FeedbackInfoMsgMqttVO infoMsgMqttVO, RcInfoCodeCfg rcInfoCodeCfg, JSONObject objectInfo) {
        if (infoMsgMqttVO != null && infoMsgMqttVO.getDataStatus() != null && infoMsgMqttVO.getDataStatus() == 1) {
            rcInfoCodeCfg.setDataValue(infoMsgMqttVO == null ? null : infoMsgMqttVO.getDataValue());
            rcInfoCodeCfg.setDataTime(infoMsgMqttVO == null ? null : infoMsgMqttVO.getDataTime());
            if (objectInfo != null) {
                objectInfo.put(rcInfoCodeCfg.getInfoCode(), infoMsgMqttVO == null ? null : infoMsgMqttVO.getDataValue());
            }
        }
    }

}




