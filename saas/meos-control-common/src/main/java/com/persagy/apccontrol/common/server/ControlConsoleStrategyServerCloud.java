package com.persagy.apccontrol.common.server;

import com.persagy.apccontrol.basic.abstracts.AbstractControlConsoleStrategyServer;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.entity.EquipRecord;
import com.persagy.apccontrol.basic.model.enumtype.RunStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdPersistenceTimeTypeEnum;
import com.persagy.apccontrol.basic.model.param.strategy.chiller.ChillerRunningStrategyQueryParam;
import com.persagy.apccontrol.basic.model.param.strategy.chiller.HotColdStationRunningStrategyQueryParam;
import com.persagy.apccontrol.basic.model.param.strategy.lastfreezepumpgroup.LastFreezePumpGroupRunningStrategyQueryParam;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.strategy.RunningStrategyVO;
import com.persagy.apccontrol.basic.model.vo.strategy.chiller.ChillerUnitRunningPlanVO;
import com.persagy.apccontrol.basic.model.vo.strategy.chiller.HotColdStationUnitRunningPlanVO;
import com.persagy.apccontrol.basic.model.vo.strategy.chiller.LastFreezePumpGroupRunningPlanVO;
import com.persagy.apccontrol.basic.service.param.equiprecord.EquipRecordQueryDaoParam;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.apccontrol.common.service.strategy.HotColdStationStrategyService;
import com.persagy.apccontrol.common.service.strategy.StrategyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 边缘端控制台策略
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/10/31 17:43
 * @since v5.241101.0
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ControlConsoleStrategyServerCloud extends AbstractControlConsoleStrategyServer {
    private final StrategyService strategyService;
    private final HotColdStationStrategyService hotColdStationStrategyService;
    private final AbstractIotDataCacheServer abstractIotDataCacheServer;


    @Override
    public RunningStrategyVO<ChillerUnitRunningPlanVO> listChillerRunningStrategy(
            @NotNull ChillerRunningStrategyQueryParam param) {
        return strategyService.listChillerRunningStrategy(param);
    }

    @Override
    public List<RunningStrategyVO<LastFreezePumpGroupRunningPlanVO>> listLastFreezePumpGroupRunningStrategy(
            LastFreezePumpGroupRunningStrategyQueryParam param) {
        return strategyService.listLastFreezePumpGroupRunningStrategy(param);
    }

    @Override
    public RunningStrategyVO<HotColdStationUnitRunningPlanVO> listChillerRunningStrategy(
            HotColdStationRunningStrategyQueryParam param) {
        return hotColdStationStrategyService.listChillerRunningStrategy(param);
    }

    @Override
    public Map<String, RunStatusEnum> mapRecentRunningStatus(EquipRecordQueryDaoParam equipRecordQueryDaoParam) {
        LocalDateTime fiveMinutePrecisionTime = DateUtils.getTheLastPrecisionMinutes(5);
        LocalDateTime tenMinutesAgo = fiveMinutePrecisionTime.minusMinutes(5);
        String tenMinutesAgoString = DateUtils.localDateTimeToString(tenMinutesAgo);
        String fiveMinutePrecisionTimeString = DateUtils.localDateTimeToString(fiveMinutePrecisionTime);
        Collection<String> systemIds = equipRecordQueryDaoParam.getObjIds();
        List<InfoCodeValueVO> infoCodeValues = abstractIotDataCacheServer.queryObjectIdPersistentDataList(
                DtpRwdPersistenceTimeTypeEnum.FIVE_MINUTES,
                systemIds,
                Collections.singletonList(InfoCodeEnum.RUN_STATUS.value()),
                tenMinutesAgoString,
                fiveMinutePrecisionTimeString, true);

        HashMap<String, RunStatusEnum> resultMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(infoCodeValues)) {
            infoCodeValues.forEach(infoCodeValueVO->{
                RunStatusEnum runStatusEnum = null;
                Object infoCodeValue = infoCodeValueVO.getInfoCodeValue();
                if (Objects.nonNull(infoCodeValue)) {
                    runStatusEnum = StringUtils.equals(String.valueOf(infoCodeValue), "1")
                            ? RunStatusEnum.OPEN : RunStatusEnum.CLOSE;
                }
                resultMap.put(infoCodeValueVO.getObjectId(), runStatusEnum);
            });

        }
        return resultMap;
    }

    @Override
    public List<EquipRecord> listEquipRecord(EquipRecordQueryDaoParam equipRecordQueryDaoParam) {
        return new ArrayList<>();
    }

}
