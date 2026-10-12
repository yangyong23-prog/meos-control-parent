package com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.impl;

import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataModel;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdPersistenceTimeTypeEnum;
import com.persagy.apccontrol.basic.model.vo.infocode.InfoCodeFigureVO;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.InfoCodeAnalyzeStrategy;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * IOT统计信息点解析策略
 *
 * @author JiYinchuan
 * @since 5.240702.0
 */
@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "rawtypes"})
@Component(IotCloudStatisticsInfoCodeAnalyzeStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class IotCloudStatisticsInfoCodeAnalyzeStrategy
        extends BaseIotInfoCodeAnalyzeStrategy
        implements InfoCodeAnalyzeStrategy {

    public static final String BEAN_NAME = "IotCloudStatisticsInfoCodeAnalyzeStrategy";

    private final AbstractCommonServer abstractCommonServer;
    private final AbstractIotDataCacheServer abstractIotDataCacheServer;

    @Override
    public InfoCodeTypeEnum infoCodeType() {
        return InfoCodeTypeEnum.IOT_CLOUD_STATISTIC;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void fillInfoCodeDataModel(
            @NonNull List<InfoCodeAggregate> aggregates,
            @Nullable Map<String, Object> additionalValues) {
        if (CollectionUtils.isEmpty(aggregates)) {
            return;
        }
        Set<String> spaceIds = aggregates.stream()
                .map(item -> item.getIdentifier().getObject().getId())
                .collect(Collectors.toSet());
        Set<String> infoCodeList = aggregates.stream()
                .map(item -> item.getConfigModel().getInfoCode())
                .collect(Collectors.toSet());

        // iot数据查询
        Map<String, Map<String, Map<String, String>>> infoCodeMapGroupingBySpaceId =
                this.abstractCommonServer.queryObjectIdInfoCodeHistory(DtpRwdPersistenceTimeTypeEnum.FIVE_MINUTES, spaceIds, infoCodeList);

        // 云边取值范围不一样 云端24小时，边缘端2小时
        LinkedList<LocalDateTime> fiveMinutesWithTimeRange = DateUtils.getFiveMinutesWithTimeRange(288);

        // 重组数据响应前端
        for (InfoCodeAggregate aggregate : aggregates) {
            String infoCode = aggregate.getConfigModel().getInfoCode();
            String spaceId = aggregate.getIdentifier().getObject().getId();
            LinkedList<InfoCodeFigureVO> resultList = new LinkedList<>();
            if (infoCodeMapGroupingBySpaceId.containsKey(spaceId)) {
                Map<String, Map<String, String>> infoCodeMap = infoCodeMapGroupingBySpaceId.get(spaceId);
                InfoCodeFigureVO.mapToResultList(infoCodeMap.getOrDefault(infoCode, new HashMap<>(16)), fiveMinutesWithTimeRange, resultList);
            }
            aggregate.setInfoCodeAndDataModel(new InfoCodeDataModel(infoCode, resultList, null, null));
        }
    }


    @Override
    public String logTag() {
        return "信息点配置-信息点解析策略-IOT信息点统计";
    }
}
