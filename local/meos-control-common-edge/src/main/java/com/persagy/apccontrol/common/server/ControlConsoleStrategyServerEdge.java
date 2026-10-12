package com.persagy.apccontrol.common.server;

import com.persagy.apccontrol.basic.abstracts.AbstractControlConsoleStrategyServer;
import com.persagy.apccontrol.basic.model.entity.EquipRecord;
import com.persagy.apccontrol.basic.model.enumtype.RunStatusEnum;
import com.persagy.apccontrol.basic.model.param.strategy.chiller.ChillerRunningStrategyQueryParam;
import com.persagy.apccontrol.basic.model.param.strategy.chiller.HotColdStationRunningStrategyQueryParam;
import com.persagy.apccontrol.basic.model.param.strategy.lastfreezepumpgroup.LastFreezePumpGroupRunningStrategyQueryParam;
import com.persagy.apccontrol.basic.model.vo.strategy.RunningStrategyVO;
import com.persagy.apccontrol.basic.model.vo.strategy.chiller.ChillerUnitRunningPlanVO;
import com.persagy.apccontrol.basic.model.vo.strategy.chiller.HotColdStationUnitRunningPlanVO;
import com.persagy.apccontrol.basic.model.vo.strategy.chiller.LastFreezePumpGroupRunningPlanVO;
import com.persagy.apccontrol.basic.service.EquipRecordService;
import com.persagy.apccontrol.basic.service.param.equiprecord.EquipRecordQueryDaoParam;
import com.persagy.apccontrol.common.service.HotColdStationStrategyService;
import com.persagy.apccontrol.common.service.StrategyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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
public class ControlConsoleStrategyServerEdge extends AbstractControlConsoleStrategyServer {

    private final StrategyService strategyService;
    private final HotColdStationStrategyService hotColdStationStrategyService;
    private final EquipRecordService equipRecordService;

    @Override
    public RunningStrategyVO<ChillerUnitRunningPlanVO> listChillerRunningStrategy(@NotNull ChillerRunningStrategyQueryParam param) {
        return strategyService.listChillerRunningStrategy(param);
    }

    @Override
    public List<RunningStrategyVO<LastFreezePumpGroupRunningPlanVO>> listLastFreezePumpGroupRunningStrategy(LastFreezePumpGroupRunningStrategyQueryParam param) {
        return strategyService.listLastFreezePumpGroupRunningStrategy(param);
    }

    @Override
    public RunningStrategyVO<HotColdStationUnitRunningPlanVO> listChillerRunningStrategy(HotColdStationRunningStrategyQueryParam param) {
        return hotColdStationStrategyService.listChillerRunningStrategy(param);
    }

    @Override
    public Map<String, RunStatusEnum> mapRecentRunningStatus(EquipRecordQueryDaoParam equipRecordQueryDaoParam) {
        return equipRecordService.mapRecentRunningStatus(equipRecordQueryDaoParam);
    }

    @Override
    public List<EquipRecord> listEquipRecord(EquipRecordQueryDaoParam equipRecordQueryDaoParam) {
        return equipRecordService.list(equipRecordQueryDaoParam);
    }

}
