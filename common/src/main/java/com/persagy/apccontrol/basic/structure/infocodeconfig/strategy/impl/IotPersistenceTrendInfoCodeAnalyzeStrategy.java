package com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.impl;

import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataModel;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdPersistenceTimeTypeEnum;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.infocode.InfoCodeFigureVO;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.dto.LoopConditions;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.InfoCodeAnalyzeStrategy;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 持久化数值趋势图
 *
 * @author LiuYang
 * @since 5.240802.0
 */
@Slf4j
@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "rawtypes"})
@Component(IotPersistenceTrendInfoCodeAnalyzeStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class IotPersistenceTrendInfoCodeAnalyzeStrategy
        extends BaseIotInfoCodeAnalyzeStrategy
        implements InfoCodeAnalyzeStrategy {

    public static final String BEAN_NAME = "iotPersistenceTrendInfoCodeAnalyzeStrategy";

    private final AbstractIotDataCacheServer abstractIotDataCacheServer;

    @Override
    public InfoCodeTypeEnum infoCodeType() {
        return InfoCodeTypeEnum.IOT_PERSISTENCE_TREND;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void fillInfoCodeDataModel(
            @NonNull List<InfoCodeAggregate> aggregates,
            @Nullable Map<String, Object> additionalValues) {
        // 时间类型
        DtpRwdPersistenceTimeTypeEnum timeTypeEnum = aggregates.stream()
                .map(item -> item.getConfigModel().parseTackValueLogicModelByPersistenceTimeType().getTimeType())
                .distinct().collect(Collectors.toList()).get(0);
        // 对象类型
        Set<String> objectIdList = aggregates.stream()
                .map(item -> item.getIdentifier().getObject().getId())
                .collect(Collectors.toSet());
        // 信息点
        Set<String> infoCodeList = aggregates.stream()
                .map(item -> item.getIdentifier().getInfoCode())
                .collect(Collectors.toSet());

        // 根据时间类型返回的循环条件
        LoopConditions loopConditions = DtpRwdPersistenceTimeTypeEnum.timeTypeToTimeComplementConditions(timeTypeEnum);

        String startTime = DateUtils.getStartTimeOfDayString(DateUtils.getDateNow());
        String endTime = DateUtils.getNowTimeString();
        // 云边持久化数据
        List<InfoCodeValueVO> infoCodeValueList = this.abstractIotDataCacheServer.queryObjectIdPersistentDataList(
                timeTypeEnum, objectIdList, infoCodeList, startTime, endTime, true);
        HashMap<String, HashMap<String, LinkedList<InfoCodeFigureVO>>> objectInfoCodeValueListMap =
                persistentDataByLookConditions(infoCodeValueList, loopConditions);

        // 重组数据响应前端
        for (InfoCodeAggregate aggregate : aggregates) {
            String objectId = aggregate.getIdentifier().getObject().getId();
            String infoCode = aggregate.getConfigModel().getInfoCode();
            List<InfoCodeFigureVO> resultList = new ArrayList<>();
            if (objectInfoCodeValueListMap.containsKey(objectId)) {
                HashMap<String, LinkedList<InfoCodeFigureVO>> calculateMap = objectInfoCodeValueListMap.get(objectId);
                resultList = calculateMap.getOrDefault(infoCode, new LinkedList<>());
            }
            aggregate.setInfoCodeAndDataModel(new InfoCodeDataModel(infoCode, resultList, null, null));
        }
    }


    /**
     * 解析持久化数据
     *
     * @param infoCodeValueList 持久化数据
     * @return 需求状态
     * @since 5.240802.0
     */
    private HashMap<String, HashMap<String, LinkedList<InfoCodeFigureVO>>> persistentDataByLookConditions(
            @NonNull List<InfoCodeValueVO> infoCodeValueList,
            LoopConditions loopConditions) {

        LinkedHashMap<String, HashMap<String, LinkedList<InfoCodeFigureVO>>> objectObjectLinkedHashMap = new LinkedHashMap<>();
        if (CollectionUtils.isNotEmpty(infoCodeValueList)) {

            // <对象id,<信息点<信息点时间,信息点数值>>>
            Map<String, Map<String, Map<LocalDateTime, Object>>> objectIdInfoCodeTimeValueMap =
                    InfoCodeValueVO.listToObjectIdInfoCodeTimeValueMap(infoCodeValueList);

            objectIdInfoCodeTimeValueMap.forEach((objectId, infoCodeMap) -> {
                        // 信息点分组后的数据转成24小时数组
                        HashMap<String, LinkedList<InfoCodeFigureVO>> objectObjectHashMap = new HashMap<>(16);
                        infoCodeMap.forEach((infoCode, dateValueMap) -> {
                            LinkedList<InfoCodeFigureVO> allTimes = new LinkedList<>();
                            LocalDateTime startDateTime = DateUtils.now().withNano(0).withSecond(0).withMinute(0).withHour(0);

                            // 补齐24小时数据
                            for (int i = loopConditions.getStart(); i <= loopConditions.getEnd(); i++) {
                                allTimes.add(new InfoCodeFigureVO(startDateTime, dateValueMap.getOrDefault(startDateTime, null)));
                                startDateTime = startDateTime.plusHours(1);
                            }
                            objectObjectHashMap.put(infoCode, allTimes);
                        });
                        objectObjectLinkedHashMap.put(objectId, objectObjectHashMap);
                    }
            );
        }

        return objectObjectLinkedHashMap;
    }

    @Override
    public String logTag() {
        return "信息点配置-信息点解析策略-持久化iot趋势图";
    }
}
