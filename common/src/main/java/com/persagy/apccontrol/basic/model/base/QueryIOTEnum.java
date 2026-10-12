package com.persagy.apccontrol.basic.model.base;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 查询iot数据的枚举类
 */
@AllArgsConstructor
@Getter
public enum QueryIOTEnum implements BaseEnum<String>, IEnum<String> {


    /**
     * 电表
     */
    AMBIENT_TEMP("key_ea4f3d4c573a45288a838d56cc8ba5c2", "WESEMS", "key_d2cb532b132b4f3ca600a484e1379ef0", "key_23e5dd65e6d34be4b01b27d250ea59db","ambientTemp","℃"),
    AMBIENT_RH("key_ea4f3d4c573a45288a838d56cc8ba5c2", "WESEMS", "key_245a1f1c05844e85ba4536d0c50325b1", "key_be2f2bf90b0241c6adbdd5373477dde6","ambientRH","%RH"),
    T_W_B("key_ea4f3d4c573a45288a838d56cc8ba5c2", "WESEMS", "key_91c0ed838786402db5fc05e182a47f6a", "key_8ef7910682e84209947dcf7e90b66738","twb","℃"),
    H("key_ea4f3d4c573a45288a838d56cc8ba5c2", "WESEMS", "key_5b6397f82fc544a1b92dfc8ce6f93230", "key_be68eae927d5446085110725fad6cf66","h","kJ/kg.dra"),
;
    /**
     * 设备类名称
     */
    private final String deviceName;

    /**
     * 设备编码
     */
    private final String deviceCoding;

    /**
     * 监测数据
     */
    private final String monitoringData;


    /**
     * 信息点名称
     */
    private final String infoName;
    /**
     * 信息点编码
     */
    private final String infoCode;

    /**
     * 单位
     */
    private final String unit;

    @Override
    public String getValue() {
        return infoCode;
    }

    @Override
    public String value() {
        return infoCode;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(infoName);
    }

    public String getDeviceName() {
        return LocaleMessageUtil.getMessage(deviceName);
    }

    public String getMonitoringData() {
        return LocaleMessageUtil.getMessage(monitoringData);
    }

    public String getInfoName() {
        return LocaleMessageUtil.getMessage(infoName);
    }


    /**
     * 拿到所有的信息点编码
     * @return
     */
    public static List<String> getInfoCodes() {
        List<String> infoCodes = CollectionUtil.newArrayList();
        for (QueryIOTEnum value : QueryIOTEnum.values()) {
            infoCodes.add(value.getInfoCode());
        }
        return infoCodes;
    }

    /**
     * 根据code拿到对应枚举
     * @param infoCode
     * @return
     */
    public static QueryIOTEnum getQueryIOTEnumByinfoCode(String infoCode) {
        for (QueryIOTEnum value : QueryIOTEnum.values()) {
            if (value.getInfoCode().equals(infoCode)) {
                return value;
            }
        }
        return null;
    }
}
