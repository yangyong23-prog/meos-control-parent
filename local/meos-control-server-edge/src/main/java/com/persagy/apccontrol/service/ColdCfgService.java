package com.persagy.apccontrol.service;

import com.persagy.apccontrol.common.model.param.CoolWaterReturnTempCfgParam;
import com.persagy.apccontrol.common.model.param.ExtendInfoCodeValueConfigParam;
import com.persagy.apccontrol.common.model.param.OutsideCoolWaterReturnTempCfgParam;

/**
 * 冷战配置相关业务
 */
public interface ColdCfgService {


    /**
     * 冷却塔出水温度设置
     *
     * @param param 冷却塔出水温度设置参数
     */
    public void coolWaterReturnTempCfg(CoolWaterReturnTempCfgParam param);
    
    /**
     * 外部接口设置-冷却塔出水温度配置
     *
     * @param param
     */
    public void outsideCoolWaterReturnTempCfg(OutsideCoolWaterReturnTempCfgParam param);
    
    /**
     * 供冷系统扩展信息点值配置
     *
     * @param param 设定参数
     */
    public void extendInfoCodeValueConfig(ExtendInfoCodeValueConfigParam param);


}
