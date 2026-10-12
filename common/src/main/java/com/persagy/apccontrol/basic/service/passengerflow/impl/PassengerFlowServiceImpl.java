package com.persagy.apccontrol.basic.service.passengerflow.impl;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdPersistenceTimeTypeEnum;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.passengerflow.ClientServerHomeStatistics;
import com.persagy.apccontrol.basic.model.vo.passengerflow.ClientServerTendencyFigure;
import com.persagy.apccontrol.basic.model.vo.passengerflow.PassengerFlowHomeStatistics;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.passengerflow.PassengerFlowService;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 客流
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/8/21 14:52
 * @since v5.240802.0
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PassengerFlowServiceImpl implements PassengerFlowService, LogComponent {

    private final DtpRwdClientService dtpRwdClientService;
    private final AbstractIotDataCacheServer abstractIotDataCacheServer;

    /**
     * 客流首页上方统计
     *
     * @return 统计后的结果集
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<PassengerFlowHomeStatistics> getPassagewayHeadCount() {
        String projectId = ContextUtil.getProjectId();
        DtpRwdObjectQueryParam dtpRwdObjectQueryParam = new DtpRwdObjectQueryParam();
        dtpRwdObjectQueryParam.setWithColumns(Lists.newArrayList(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.localName));
        List<DtpRwdEquipmentDTO> gateGroupList = getGateGroupList(dtpRwdObjectQueryParam);
        List<PassengerFlowHomeStatistics> resultStatistics = new LinkedList<>();

        // 1. 收集所有待检查对象 ID（项目 + 门组）
        Set<String> allObjectIds = new LinkedHashSet<>();
        allObjectIds.add(projectId);
        if (CollectionUtils.isNotEmpty(gateGroupList)) {
            gateGroupList.forEach(item -> allObjectIds.add(item.getId()));
        }

        // 2. 按客流信息点实例化规则过滤（并集：任意一个 infoCode 有规则即保留）
        Set<String> validObjectIds = filterByPassengerFlowRule(allObjectIds);
        boolean projectHasRule = validObjectIds.contains(projectId);
        logProjectInstance().info("客流规则过滤", Arrays.asList(
                Pair.of("过滤前门组数", gateGroupList.size()),
                Pair.of("过滤后有效对象数", validObjectIds.size()),
                Pair.of("项目是否有规则", projectHasRule)
        ));

        if (CollectionUtils.isEmpty(validObjectIds)) {
            return resultStatistics;
        }

        // 3. 构建 objectId → name 映射（项目在首位）
        Map<String, String> objectIdNameMap = new LinkedHashMap<>();
        if (projectHasRule) {
            objectIdNameMap.put(projectId, LocaleMessageUtil.getMessage("key_c8ff811cb5234f69b587f5dd1fa73141"));
        }
        if (CollectionUtils.isNotEmpty(gateGroupList)) {
            gateGroupList.stream()
                    .filter(item -> validObjectIds.contains(item.getId()))
                    .forEach(item -> objectIdNameMap.put(item.getId(), item.getLocalName()));
        }

        // 4. 查询 IOT 数据
        Set<String> queryObjectIds = objectIdNameMap.keySet();
        List<InfoCodeValueVO> infoCodeDayList = this.abstractIotDataCacheServer.listInfoCodeValueInOneHour(
                queryObjectIds, CUMULATIVE_PERSON_TIME_INFO_CODE_LIST);
        Map<String, Map<String, Map<LocalDateTime, Object>>> infoCodeHoverMap = this.abstractIotDataCacheServer.queryObjectIdPersistentDataMap(
                DtpRwdPersistenceTimeTypeEnum.HOUR, queryObjectIds, GATE_GROUP_INFO_CODE_LIST);
        Map<String, List<InfoCodeValueVO>> infoCodeDayMap = new HashMap<>(16);
        if (CollectionUtils.isNotEmpty(infoCodeDayList)) {
            infoCodeDayMap = infoCodeDayList.stream().collect(Collectors.groupingBy(InfoCodeValueVO::getObjectId));
        }
        Map<String, List<InfoCodeValueVO>> finalInfoCodeDayMap = infoCodeDayMap;
        objectIdNameMap.forEach((objectId, objectName) ->
                resultStatistics.add(PassengerFlowHomeStatistics.iotToStatistics(
                        objectId, objectName,
                        finalInfoCodeDayMap.getOrDefault(objectId, new ArrayList<>()),
                        infoCodeHoverMap.getOrDefault(objectId, new HashMap<>(16)))));

        return resultStatistics;
    }

    /**
     * 获取所有的门组
     *
     * @param param 查询rwd的入参，可以传递空间，仅返回空间下的数据
     * @return 门组对象
     */
    public List<DtpRwdEquipmentDTO> getGateGroupList(DtpRwdObjectQueryParam param) {
        param.setClassCodeEnum(DtpRwdClassCodeEnum.CFDRG);
        return dtpRwdClientService.listEquipment(param);
    }

    /**
     * 按客流信息点实例化规则过滤对象（并集逻辑：任意一个 infoCode 有规则即保留）
     *
     * @param objectIds 待过滤的对象 ID 集合（含项目 ID 和门组 ID）
     * @return 有规则的对象 ID 集合
     * @since v7.260802.0
     */
    private Set<String> filterByPassengerFlowRule(Set<String> objectIds) {
        if (CollectionUtils.isEmpty(objectIds)) {
            return Collections.emptySet();
        }
        Map<String, Set<String>> classCodeAndObjectIds = Collections.singletonMap(
                DtpRwdClassCodeEnum.CFDRG.value(), objectIds);
        Map<String, Set<String>> objectIdAndExistsRuleInfoCodes =
                abstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes(
                        classCodeAndObjectIds, CUMULATIVE_PERSON_TIME_INFO_CODE_LIST);
        return objectIdAndExistsRuleInfoCodes.entrySet().stream()
                .filter(entry -> CollectionUtils.containsAny(
                        entry.getValue(), CUMULATIVE_PERSON_TIME_INFO_CODE_LIST))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    /**
     * cs首页客流人数统计
     *
     * @return 30内的日维度数据
     */
    @Override
    @SuppressWarnings("unchecked")
    public ClientServerHomeStatistics homeStatistics() {
        LocalDateTime pastTime = DateUtils.getPastTime(30);
        String startTime = DateUtils.localDateTimeToString(pastTime);
        String endTime = DateUtils.getNowTimeString();
        // 30天日维度的所有数据
        List<InfoCodeValueVO> infoCodeDayList = this.abstractIotDataCacheServer.queryObjectIdPersistentDataList(
                DtpRwdPersistenceTimeTypeEnum.DAY,
                Collections.singleton(ContextUtil.getProjectId()),
                Collections.singleton(ACCD_IN_PEOPLE_FLOW_STRING), startTime, endTime, true);
        ClientServerHomeStatistics clientServerHomeStatistics = new ClientServerHomeStatistics();
        if (CollectionUtils.isEmpty(infoCodeDayList)) {
            return clientServerHomeStatistics;
        }
        // 信息点数据统计，总数
        long totalNumberOfPeople = 0L;
        List<ClientServerTendencyFigure> clientServerTendencyFigures = new LinkedList<>();
        Map<LocalDateTime, Long> localDateTimeValueMap = infoCodeDayList.stream()
                .filter(x -> Objects.nonNull(x.getInfoCodeValue()))
                .collect(Collectors.toMap(
                        InfoCodeValueVO::getUpdateTime,
                        item -> {
                            try {
                                double numberOfPeople = Double.parseDouble(String.valueOf(item.getInfoCodeValue()));
                                return Math.round(numberOfPeople);
                            } catch (Exception e) {
                                logProjectInstance().error("cs客流首页参数转换异常", e, Pair.of("客流天维度数据异常item", item));
                            }
                            return 0L;
                        }
                ));
        for (int i = 0; i < 30; i++) {
            Long infoCodeValue = null;
            if (localDateTimeValueMap.containsKey(pastTime)) {
                infoCodeValue = localDateTimeValueMap.get(pastTime);
                totalNumberOfPeople += infoCodeValue;
            }
            clientServerTendencyFigures.add(new ClientServerTendencyFigure(infoCodeValue, pastTime));
            pastTime = pastTime.plusDays(1);
        }
        clientServerHomeStatistics.setGraph(clientServerTendencyFigures);
        long roundedValue = Math.round((double) totalNumberOfPeople / 30);
        clientServerHomeStatistics.setAverageNumberOfPeople(roundedValue);
        LocalDateTime todayStart = DateUtils.getTodayStart();
        if (localDateTimeValueMap.containsKey(todayStart)) {
            clientServerHomeStatistics.setTodayTotalNumberOfPeople(localDateTimeValueMap.get(todayStart));
        }
        return clientServerHomeStatistics;
    }

    /**
     * 获取日志标签
     *
     * @return 日志标签
     * @since 5.240802.0
     */
    @Override
    public String logTag() {
        return "客流";
    }
}
