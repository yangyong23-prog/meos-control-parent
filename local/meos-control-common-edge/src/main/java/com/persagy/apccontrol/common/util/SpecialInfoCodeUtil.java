package com.persagy.apccontrol.common.util;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.model.base.DashBoardInfoCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.AutoMaticInfoCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.system.SysResponseCode;
import com.persagy.framework.common.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 特殊信息点处理
 *
 * @author yuyao
 */
public class SpecialInfoCodeUtil {



    /**
     * 特殊信息点数据获取  冷机关闭指令信息
     * chillerSNumber   小冷机数量
     * chillerLNumber  大冷机数量
     */
    public static Map<String, Integer> getChillerNum() {
        Map<String, Integer> map = new HashMap<>();
        map.put(DashBoardInfoCodeEnum.CHILLER_L_NUM.getCode(), 0);
        map.put(DashBoardInfoCodeEnum.CHILLER_S_NUM.getCode(), 0);
        return map;
    }

    public static String getSysInfoCodeByClassCode(String classCode, String infoCode) {
        if (AutoMaticInfoCodeEnum.RUN_STATUS.getCode().equals(infoCode)) {
            if (StringUtils.equals(classCode, DtpRwdClassCodeEnum.COLD_SYSTEM.getValue()) ||
                    StringUtils.equals(classCode, DtpRwdClassCodeEnum.ACCI_SYSTEM.getValue())
            ) {
                infoCode = DashBoardInfoCodeEnum.SYS_SWITCH_SET.getCode();
            }
            if (StringUtils.equals(classCode, DtpRwdClassCodeEnum.ACCPC_GROUP.getValue())) {
                infoCode = DashBoardInfoCodeEnum.ENABLE_CTRL.getCode();
            }
        }
        return infoCode;
    }


    /**
     * 获取系统 或者泵组的 开关机状态信息点
     *
     * @param classCode
     * @return
     */
    public static String getRunStatusSysByClassCode(String classCode) {
        if (StringUtils.equals(classCode, DtpRwdClassCodeEnum.COLD_SYSTEM.getValue()) ||
                StringUtils.equals(classCode, DtpRwdClassCodeEnum.ACCI_SYSTEM.getValue())
        ) {
            return DashBoardInfoCodeEnum.SYS_SWITCH_SET.getCode();
        }
        if (StringUtils.equals(classCode, DtpRwdClassCodeEnum.ACCPC_GROUP.getValue())) {
            return DashBoardInfoCodeEnum.ENABLE_CTRL.getCode();
        }
        throw new BusinessException(SysResponseCode.FAIL_K0001.getCode(), LocaleMessageUtil.getMessage("key_bb73b37b7b7048968897a3c8b1297806") + classCode);
    }

    public static List<AutoMaticInfoCodeEnum> getAutoMaticInfoCodeEnumByClassCode(String classCode) {
        if (StringUtils.equals(classCode, DtpRwdClassCodeEnum.COLD_SYSTEM.getValue())) {
            //冷机信息点
            return AutoMaticInfoCodeEnum.getInfoCodeList("1");
        }
        if (StringUtils.equals(classCode, DtpRwdClassCodeEnum.ACCPC_GROUP.getValue())) {
            //冷冻水泵控制组
            return AutoMaticInfoCodeEnum.getInfoCodeList("2");
        }
        if (StringUtils.equals(classCode, DtpRwdClassCodeEnum.ACCI_SYSTEM.getValue())) {
            //冷热源系统
            return AutoMaticInfoCodeEnum.getInfoCodeList("3");
        }
        if (StringUtils.equals(classCode, DtpRwdClassCodeEnum.HOT_SYSTEM.getValue())) {
            //冷热源系统
            return AutoMaticInfoCodeEnum.getInfoCodeList("5");
        }
        return null;
    }

    public static String getSysInfoCodeByVal(String val) {

        if (RunStatusEnum.CLOSE.value().equals(val)) {
            return "0";
        }
        if (RunStatusEnum.OPEN.value().equals(val)) {
            return "1";
        }

        return val;
    }


}
