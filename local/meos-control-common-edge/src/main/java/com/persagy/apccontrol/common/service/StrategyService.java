package com.persagy.apccontrol.common.service;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFreezePumpDTO;
import com.persagy.apccontrol.basic.model.entity.EquipRecord;
import com.persagy.apccontrol.basic.model.param.dashboard.StrategyAupportAiQueryParam;
import com.persagy.apccontrol.basic.model.param.strategy.StrategyFreezeGroupObjectQueryParam;
import com.persagy.apccontrol.basic.model.param.strategy.chiller.ChillerRunningStrategyQueryParam;
import com.persagy.apccontrol.basic.model.param.strategy.lastfreezepumpgroup.LastFreezePumpGroupRunningStrategyQueryParam;
import com.persagy.apccontrol.basic.model.vo.ColdSiteSystemContrastVO;
import com.persagy.apccontrol.basic.model.vo.strategy.RunningStrategyVO;
import com.persagy.apccontrol.basic.model.vo.strategy.RunningValueVO;
import com.persagy.apccontrol.basic.model.vo.strategy.chiller.ChillerUnitRunningPlanVO;
import com.persagy.apccontrol.basic.model.vo.strategy.chiller.LastFreezePumpGroupRunningPlanVO;
import com.persagy.apccontrol.basic.model.vo.strategy.chiller.RunningCoolerVO;
import com.persagy.apccontrol.common.model.param.strategy.StrategyQueryParam;
import com.persagy.apccontrol.common.model.param.strategy.StrategyTypeChangeInsertParam;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * 策略干预服务
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
public interface StrategyService {

    // ------------------------- 公共 -------------------------


    /**
     * 查询末级泵组策略对象
     *
     * @param param 查询参数
     * @return 末级泵组策略对象
     * @since v5.6.0
     */
    List<DtpRwdFreezePumpDTO> listLastFreezePumpGroups(@NonNull StrategyFreezeGroupObjectQueryParam param);

    /**
     * 查询运行的冷机数据
     *
     * @param projectId 系统ID
     * @param systemId  系统ID
     * @return 冷机台数
     * @since v5.6.0
     */
    RunningCoolerVO getRunningChillerData(String projectId, String systemId);

    /**
     * 查询设备额定功率
     *
     * @param groupCode 集团编码
     * @param projectId 项目ID
     * @param systemId  系统ID
     * @return 设备额定功率
     * @since v5.6.0
     */
    List<Integer> getRate(String groupCode, String projectId, String systemId);


    // ------------------------- 冷机策略 -------------------------

    /**
     * 查询冷机运行策略列表
     *
     * @param param 查询参数
     * @return 冷机运行策略列表
     * @since v5.6.0
     */
    RunningStrategyVO<ChillerUnitRunningPlanVO> listChillerRunningStrategy(@NonNull ChillerRunningStrategyQueryParam param);


    // ------------------------- 末级冷冻泵组 -------------------------

    /**
     * 查询末级泵组运行策略列表
     *
     * @param param 查询参数
     * @return 末级泵组运行策略列表
     * @since v5.6.0
     */
    List<RunningStrategyVO<LastFreezePumpGroupRunningPlanVO>> listLastFreezePumpGroupRunningStrategy(
            @NonNull LastFreezePumpGroupRunningStrategyQueryParam param);



    /**
     * 验证算法ai策略是否靠谱
     *
     * @param param
     * @return
     */
    Boolean fetchSupportAi(StrategyQueryParam param);

    /**
     * 查询当前是否是ai策略
     *
     * @param param
     * @return
     */
    List<StrategyTypeChangeInsertParam.AiStrategyStatus> querySupportAi(StrategyAupportAiQueryParam param);

    /**
     * 策略切换接口
     *
     * @param param
     * @return
     */
    Boolean saveSupportAi(StrategyTypeChangeInsertParam param);


    @SuppressWarnings("unchecked")
    <T> void analyzeRunningValue(
            @Nullable ColdSiteSystemContrastVO source,
            @NonNull Class<T> runningValueClass,
            @NonNull Consumer<RunningValueVO<T>> callback);

    <T> void fillRunningTime(@NonNull RunningStrategyVO<T> target, @NonNull List<EquipRecord> equipRecords);

    void queryManualInterventionAndFillData(
            @NonNull RunningStrategyVO<?> target, @NonNull String objectId,
            @NonNull String infoCode, @Nullable String fieldName);

    public void queryManualAutoMaticAndFillData(
            @NonNull RunningStrategyVO<?> target, @NonNull String objectId,
            @NonNull String infoCode, @Nullable String fieldName);

    public Boolean queryObjectIsRunning(String objectId);

}
