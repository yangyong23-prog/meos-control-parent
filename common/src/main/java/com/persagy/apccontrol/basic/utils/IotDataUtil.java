package com.persagy.apccontrol.basic.utils;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.base.DashBoardInfoCodeEnum;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.entity.InfoCodeInfo;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;

import java.math.BigDecimal;
import java.util.Set;

/**
 * IOT 数据，二次处理工具类
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/7/30 10:27
 */
public class IotDataUtil {

    public static final Set<String> COMMON_INFO_CODE_SET = Sets.newHashSet(InfoCodeEnum.RUN_STATUS.getCode(), InfoCodeEnum.ONLINE_STATUS.getCode(),
            InfoCodeEnum.ORDER_FAIL_ALARM.getCode(), InfoCodeEnum.FAULT_STATUS.getCode(), InfoCodeEnum.MANUAL_AUTO_STATUS.getCode(), InfoCodeEnum.LOCAL_REMOTZE_STATUS.getCode(),
            InfoCodeEnum.EQUIP_SWITCH_SET.getCode(), InfoCodeEnum.ENABLE_CTRIL.getCode(), DashBoardInfoCodeEnum.SYS_SWITCH_SET.getCode());

    /**
     * 重写IOT数据值
     *
     * @param dataValue
     * @param infoCodeCfg
     */
    public static String overrideDataValue(String infoCode, String dataValue, RcInfoCodeCfg infoCodeCfg) {
        if (StrUtil.isBlank(dataValue) || CommonConst.STRING_VALUE_INFO_CODES.contains(infoCode)) {
            return dataValue;
        }
        try {
            BigDecimal iotValue = new BigDecimal(dataValue);

            if (infoCodeCfg == null) {
                if (COMMON_INFO_CODE_SET.contains(infoCode)) {
                    iotValue = iotValue.setScale(0, BigDecimal.ROUND_HALF_UP);
                }
                return iotValue.toPlainString();
            }

            // 换算系数、保留IOT小数位计算
            if (infoCodeCfg.getScale() != null) {
                iotValue = iotValue.multiply(infoCodeCfg.getScale());
            }
            if (infoCodeCfg.getIotDecimal() != null) {
                iotValue = iotValue.setScale(infoCodeCfg.getIotDecimal(), BigDecimal.ROUND_HALF_UP);
            }

            // 修正 dataValue 值
            String resultValue = iotValue.toPlainString();
//            if (InfoCodeEnum.ORDER_FAIL_ALARM.getCode().equals(infoCodeCfg.getInfoCode()) && infoCodeCfg.getExtendInfo() != null) {
//                resultValue = getTriggerFormulaInfoCodeValue(infoCodeCfg, iotValue, infoCodeCfg.getExtendInfo().getTriggerFormulaInfo());
//            }
            return StrUtil.isBlank(resultValue) ? dataValue : resultValue;
        } catch (Exception e) {
            ControlLogUtil.error("IOT DATA VALUE OVERRIDE EXCEPTION: {}, {}", dataValue, infoCodeCfg, e);
        }
        return dataValue;
    }


    /**
     * 重新赋值 dataValue
     *
     * @param infoMsgMqttVO
     * @param rcInfoCodeCfg
     * @return
     */
    public static void overrideDataValue(FeedbackInfoMsgMqttVO infoMsgMqttVO, RcInfoCodeCfg rcInfoCodeCfg) {
        if (infoMsgMqttVO != null) {
            infoMsgMqttVO.setDataValue(getVaildIotDataValue(infoMsgMqttVO.getInfoCode(), infoMsgMqttVO.getDataValue(), rcInfoCodeCfg));
        }
    }
    /**
     * 修正 dataValue 值
     *
     * @param infoCode
     * @param dataValue
     * @param rcInfoCodeCfg
     * @return
     */
    public static String getVaildIotDataValue(String infoCode, String dataValue, RcInfoCodeCfg rcInfoCodeCfg) {
        return IotDataUtil.overrideDataValue(infoCode, dataValue, rcInfoCodeCfg);
    }
}
