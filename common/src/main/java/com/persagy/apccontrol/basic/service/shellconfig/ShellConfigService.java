package com.persagy.apccontrol.basic.service.shellconfig;

import com.persagy.apccontrol.basic.controller.shellconfig.param.*;
import com.persagy.apccontrol.basic.model.entity.ShellConfigItem;
import com.persagy.apccontrol.basic.model.vo.WeatherStationVO;

import java.util.List;

public interface ShellConfigService {

    /**
     * 改变项目的算法开关
     *
     * @param groupCode
     * @param projectId
     * @return
     */
    boolean changeAlgorithmConfig(String groupCode, String projectId, Boolean status);

    /**
     * 时序大模型策略开关
     *
     * @param param
     * @return
     */
    boolean changeLtsmConfig(LtsmConfigParam param);

    /**
     * 获取算法开关
     *
     * @param groupCode
     * @param projectId
     * @return
     */
    boolean getAlgorithmConfig(String groupCode, String projectId);

    List<LtsmConfigParam> listLtsmConfig();

    List<ShellConfigVO> query(String groupCode, QueryShellConfigParam param);

    Boolean save(String groupCode, SaveShellConfigParam param);

    ShellConfigItem queryByInfoCode(String groupCode, QueryShellConfigByInfoCodeParam param);

    List<ShellConfigItem> batchQueryConfigByObjectId(String groupCode, BatchQueryShellConfigParam param);

    EfficiencyVO queryEfficiency(BasicsAnalysisParam param);
    
    void mpcAgentCfg(List<MpcAgentCfgParam> param);
    
    List<MpcAgentCfgParam> queryMpcAgentCfg();

    /**
     * 查询项目中的气象监测站列表
     * <p>
     * 返回WESEMS设备列表，按设备名称汉字升序排序
     * </p>
     *
     * @return 气象站列表，包含设备ID和名称
     * @since 7.260501.0
     */
    List<WeatherStationVO> queryWeatherStations();

}
