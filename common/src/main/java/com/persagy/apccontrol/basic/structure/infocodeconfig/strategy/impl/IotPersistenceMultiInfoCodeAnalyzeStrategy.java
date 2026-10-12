package com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.impl;

import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataModel;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdPersistenceTimeTypeEnum;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.InfoCodeAnalyzeStrategy;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 持久化策略
 *
 * @author LiuYang
 * @since 5.240802.0
 */
@Slf4j
@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "rawtypes"})
@Component(IotPersistenceMultiInfoCodeAnalyzeStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class IotPersistenceMultiInfoCodeAnalyzeStrategy
        extends BaseIotInfoCodeAnalyzeStrategy
        implements InfoCodeAnalyzeStrategy {

    public static final String BEAN_NAME = "iotPersistenceMultiInfoCodeAnalyzeStrategy";

    private final AbstractIotDataCacheServer abstractIotDataCacheServer;

    @Override
    public InfoCodeTypeEnum infoCodeType() {
        return InfoCodeTypeEnum.IOT_PERSISTENCE_MULTI;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void fillInfoCodeDataModel(
            @NonNull List<InfoCodeAggregate> aggregates,
            @Nullable Map<String, Object> additionalValues) {
        if (CollectionUtils.isEmpty(aggregates)) {
            return;
        }

        // 对象类型
        Set<String> objectIdList = aggregates.stream()
                .map(item -> item.getIdentifier().getObject().getId())
                .collect(Collectors.toSet());
        // 信息点
        Set<String> infoCodeList = aggregates.stream()
                .map(item -> item.getIdentifier().getInfoCode())
                .collect(Collectors.toSet());

        Date day =DateUtils.getDateNow();
        String startTime = DateUtils.getStartTimeOfDayString(day);
        String endTime = DateUtils.getNowTimeString();
        // 如果入参有开始结束时间则查询指定时间的持久化数据，时间精度从信息点配置取
        if (MapUtils.isNotEmpty(additionalValues)
                && additionalValues.containsKey(TIME_RANGE)) {
            Range<ChronoLocalDateTime<?>> between = analyzeAdditionalValues(additionalValues, TIME_RANGE);
            startTime = DateUtils.localDateTimeToString(LocalDateTime.from(between.getMinimum()));
            endTime = DateUtils.localDateTimeToString(LocalDateTime.from(between.getMaximum()));
        }

        // 云边持久化数据
        Map<DtpRwdPersistenceTimeTypeEnum, List<InfoCodeValueVO>> infoCodeValueTypeMap = new HashMap<>();
        for(InfoCodeAggregate aggregate:aggregates){
            DtpRwdPersistenceTimeTypeEnum timeType = aggregate.getConfigModel().parseTackValueLogicModelByPersistenceTimeType().getTimeType();

            day = setTimeByType(timeType,day);
            startTime = DateUtils.getStartTimeOfDayString(day);
            List<InfoCodeValueVO> infoCodeValues = this.abstractIotDataCacheServer.queryObjectIdPersistentDataList(
                    timeType, objectIdList, infoCodeList, startTime, endTime, false);
            infoCodeValueTypeMap.put(timeType, infoCodeValues);
        }


        // 重组数据响应前端
        for (InfoCodeAggregate aggregate : aggregates) {
            String objectId = aggregate.getIdentifier().getObject().getId();
            String infoCode = aggregate.getConfigModel().getInfoCode();
            DtpRwdPersistenceTimeTypeEnum timeType = aggregate.getConfigModel().parseTackValueLogicModelByPersistenceTimeType().getTimeType();

            Object resultValue = null;
            Map<String, Map<String, Object>> infoCodeValueMap = InfoCodeValueVO.listToObjectInfoCodeValueMap(infoCodeValueTypeMap.get(timeType));
            // 取值后直接赋值
            if (infoCodeValueMap.containsKey(objectId)) {
                Map<String, Object> infoCodeMap = infoCodeValueMap.get(objectId);
                resultValue = infoCodeMap.getOrDefault(infoCode, "");
            }
            aggregate.setInfoCodeAndDataModel(new InfoCodeDataModel(infoCode, resultValue, null, null));
        }
    }

    private Date setTimeByType(DtpRwdPersistenceTimeTypeEnum type, Date startTime) {
        switch (type) {
            case MONTH:
                startTime = DateUtils.getFirstDayOfMonth(startTime);
                break;
            case YEAR:
                startTime = DateUtils.getFirstDayOfYear(startTime);
        }
        return startTime;
    }


    @Override
    public String logTag() {
        return "信息点配置-信息点解析策略-iot持久化数值";
    }
}
