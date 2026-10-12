package com.persagy.apccontrol.basic.abstracts;

import com.persagy.apccontrol.basic.model.entity.EquipRecord;
import com.persagy.apccontrol.basic.model.enumtype.EnumTimeType;
import com.persagy.apccontrol.basic.model.enumtype.RunStatusEnum;
import com.persagy.apccontrol.basic.model.param.strategy.chiller.ChillerRunningStrategyQueryParam;
import com.persagy.apccontrol.basic.model.param.strategy.chiller.HotColdStationRunningStrategyQueryParam;
import com.persagy.apccontrol.basic.model.param.strategy.lastfreezepumpgroup.LastFreezePumpGroupRunningStrategyQueryParam;
import com.persagy.apccontrol.basic.model.vo.strategy.ChillersNumberVO;
import com.persagy.apccontrol.basic.model.vo.strategy.InfoCodeDataTimeAndPrecisionType;
import com.persagy.apccontrol.basic.model.vo.strategy.RunningStrategyVO;
import com.persagy.apccontrol.basic.model.vo.strategy.chiller.ChillerUnitRunningPlanVO;
import com.persagy.apccontrol.basic.model.vo.strategy.chiller.HotColdStationUnitRunningPlanVO;
import com.persagy.apccontrol.basic.model.vo.strategy.chiller.LastFreezePumpGroupRunningPlanVO;
import com.persagy.apccontrol.basic.outside.dto.AcccccGroupDTO;
import com.persagy.apccontrol.basic.service.BizDtpRwdService;
import com.persagy.apccontrol.basic.service.param.equiprecord.EquipRecordQueryDaoParam;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 控制台策略抽象类
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/10/31 17:43
 * @since v5.241101.0
 */
@Slf4j
public abstract class AbstractControlConsoleStrategyServer {

    @Autowired
    protected BizDtpRwdService bizDtpRwdService;

    /**
     * 查询冷机运行策略列表
     *
     * @param param 查询参数
     * @return 冷机运行策略列表
     * @since v5.6.0
     */
    public abstract RunningStrategyVO<ChillerUnitRunningPlanVO> listChillerRunningStrategy(@NonNull ChillerRunningStrategyQueryParam param);

    /**
     * 查询末级泵组运行策略列表
     *
     * @param param 查询参数
     * @return 末级泵组运行策略列表
     * @since v5.6.0
     */
    public abstract List<RunningStrategyVO<LastFreezePumpGroupRunningPlanVO>>
    listLastFreezePumpGroupRunningStrategy(LastFreezePumpGroupRunningStrategyQueryParam param);


    /**
     * 查询实时需求运行计划-页面冷热站控制面板
     *
     * @param param
     * @return
     */
    public abstract RunningStrategyVO<HotColdStationUnitRunningPlanVO> listChillerRunningStrategy(HotColdStationRunningStrategyQueryParam param);

    /**
     * 查询实时需求运行计划-页面冷热站控制面板
     * 云端查询持久化精度数据，边缘侧查询切换记录表
     *
     * @param equipRecordQueryDaoParam 查询系统运行状态
     * @return
     */
    public abstract Map<String, RunStatusEnum> mapRecentRunningStatus(EquipRecordQueryDaoParam equipRecordQueryDaoParam);

    /**
     * 查询冷站开关时间
     * 仅有边缘端，云端暂无
     *
     * @param equipRecordQueryDaoParam 查询系统运行状态
     * @return
     */
    public abstract List<EquipRecord> listEquipRecord(EquipRecordQueryDaoParam equipRecordQueryDaoParam);

    /**
     * 获取信息点数据时间和时间精度类型
     *
     * @return 信息点数据时间和时间精度类型
     */
    public InfoCodeDataTimeAndPrecisionType getInfoCodeDataTimeAndPrecisionType() {
        LocalDateTime fiveMinutePrecisionTime = DateUtils.getTheLastPrecisionMinutes(5);
        LocalDateTime tenMinutesAgo = fiveMinutePrecisionTime.minusMinutes(5);
        InfoCodeDataTimeAndPrecisionType dataTimeAndPrecisionType = new InfoCodeDataTimeAndPrecisionType();
        dataTimeAndPrecisionType.setStartTime(tenMinutesAgo);
        dataTimeAndPrecisionType.setEndTime(fiveMinutePrecisionTime);
        List<String> ipTypes = Arrays.stream(EnumTimeType.values()).map(EnumTimeType::getName).collect(Collectors.toList());
        dataTimeAndPrecisionType.setIpTypes(ipTypes);
        return dataTimeAndPrecisionType;
    }

    /**
     * 获取当前系统交付的大小冷机数量
     *
     * @param systemId 系统id
     * @return 大小冷机数量
     */
    public ChillersNumberVO getNumberOfChillers(String systemId) {
        // 查询当前运行的冷机台数
        AcccccGroupDTO acccacGroup = bizDtpRwdService.queryAcccccGroupBySystemId(ContextUtil.getGroupCode(), ContextUtil.getProjectId(), systemId);
        List<String> small = acccacGroup.getSmall();
        List<String> large = acccacGroup.getLarge();
        ChillersNumberVO chillersNumberVO = new ChillersNumberVO();
        chillersNumberVO.setLargeChillersNumber(CollectionUtils.isEmpty(large) ? 0 : large.size());
        chillersNumberVO.setSmallChillersNumber(CollectionUtils.isEmpty(small) ? 0 : small.size());
        return chillersNumberVO;
    }

}
