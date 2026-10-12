package com.persagy.apccontrol.basic.service.duty.strategy;

import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdPersistenceTimeTypeEnum;
import com.persagy.apccontrol.basic.model.param.duty.HomePageParam;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.duty.CardTagVO;
import com.persagy.apccontrol.basic.model.vo.duty.PassengerFlowCardDataVO;
import com.persagy.apccontrol.basic.model.vo.infocode.InfoCodeFigureVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.persagy.apccontrol.basic.model.base.InfoCodeEnum.ONLINE_STATUS;

/**
 * 客流值班首页策略实现类
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/12/15 12:45
 * @since v5.241202.0
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PassengerFlowHomePageCardDataHandler extends AbstractHomePageCardDataHandler<PassengerFlowCardDataVO> implements LogComponent {

    private final DtpRwdClientService dtpRwdClientService;
    private final AbstractCommonServer abstractCommonServer;
    private final AbstractIotDataCacheServer abstractIotDataCacheServer;

    @NotNull
    @Override
    public RunningMonitorTypeEnum getRunningMonitorType() {
        return RunningMonitorTypeEnum.PASSENGER_FLOW;
    }

    @Override
    @SuppressWarnings({"DuplicatedCode", "unchecked"})
    public PassengerFlowCardDataVO execute(HomePageParam param) {
        PassengerFlowCardDataVO passengerFlowCardDataVO = new PassengerFlowCardDataVO();
        passengerFlowCardDataVO.setRunningMonitorType(getRunningMonitorType());
        String projectId = ContextUtil.getProjectId();

        // iot数据查询
        String infoCode = InfoCodeEnum.ACCM_IN_PEOPLE_FLOW.value();
        List<String> objectIds = Collections.singletonList(projectId);
        List<String> infoCodeList = Collections.singletonList(infoCode);
        Map<String, Map<String, Map<String, String>>> infoCodeMapGroupingByProjectId =
                this.abstractIotDataCacheServer.queryObjectIdInfoCodeHistory(DtpRwdPersistenceTimeTypeEnum.FIFTEEN_MINUTES, objectIds, infoCodeList);
        List<InfoCodeValueVO> infoCodeDayList = this.abstractIotDataCacheServer.listInfoCodeValueInOneHour(objectIds, infoCodeList);

        if (CollectionUtils.isNotEmpty(infoCodeDayList)) {
            String value = String.valueOf(infoCodeDayList.get(0).getInfoCodeValue());
            try {
                double numberOfPeople = Double.parseDouble(value);
                passengerFlowCardDataVO.setTodayTotalNumberOfPeople(Math.round(numberOfPeople));
            } catch (Exception e) {
                logProjectInstance().error("数值转换失败", e, Pair.of("value", value));
            }
        }

        // 云边取值范围不一样 云端24小时，边缘端2小时
        LinkedList<LocalDateTime> fiveMinutesWithTimeRange = this.abstractCommonServer.getFifteenMinuteAccuracyTime();
        Map<String, String> timeValueMap = new HashMap<>();
        if (MapUtils.isNotEmpty(infoCodeMapGroupingByProjectId)) {
            timeValueMap = infoCodeMapGroupingByProjectId.get(projectId).get(infoCode);
            // 将map中的value转换为整数
            for (Map.Entry<String, String> entry : timeValueMap.entrySet()) {
                try {
                    double intValue = Double.parseDouble(entry.getValue());
                    entry.setValue(String.valueOf(Math.round(intValue)));
                } catch (NumberFormatException e) {
                    logProjectInstance().error("数值转换失败", e, Pair.of("value", entry.getValue()));
                }
            }
        }
        LinkedList<InfoCodeFigureVO> resultList = new LinkedList<>();
        InfoCodeFigureVO.mapToResultList(timeValueMap, fiveMinutesWithTimeRange, resultList);
        passengerFlowCardDataVO.setPassengerFlowFigure(resultList);

        DtpRwdObjectQueryParam dmpObjectParam = new DtpRwdObjectQueryParam();
        dmpObjectParam.setClassCodeEnum(DtpRwdClassCodeEnum.WESEPC);
        dmpObjectParam.setWithColumns(Collections.singleton(DtpRwdObjectDTO.Fields.id));
        List<DtpRwdEquipmentDTO> dtpRwdEquipments = this.dtpRwdClientService.listEquipment(dmpObjectParam);
        if (CollectionUtils.isNotEmpty(dtpRwdEquipments)) {
            CardTagVO cardTagVO = new CardTagVO();
            Set<String> equipmentIds = dtpRwdEquipments.stream()
                    .map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());

            //填充大卡片信息
            cardTagVO.setErrorEventTotal(getExceptionEventCountByObjIds(equipmentIds));
            List<InfoCodeValueVO> infoCodeValues = this.abstractIotDataCacheServer.listInfoCodeValueInOneHour(
                    equipmentIds, Collections.singleton(ONLINE_STATUS.value()));
            if (CollectionUtils.isNotEmpty(infoCodeValues)) {
                long count = infoCodeValues.stream()
                        .filter(x ->
                                Objects.nonNull(x.getInfoCodeValue()) && Objects.equals(x.getInfoCodeValue(), "0"))
                        .count();
                cardTagVO.setOfflineTotal((int) count);
            }
            passengerFlowCardDataVO.setCardTag(cardTagVO);
        }

        return passengerFlowCardDataVO;
    }

    @Override
    public String logTag() {
        return "客流运行值班首页策略";
    }
}
