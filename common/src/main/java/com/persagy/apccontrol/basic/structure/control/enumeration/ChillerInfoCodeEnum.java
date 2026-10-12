package com.persagy.apccontrol.basic.structure.control.enumeration;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 冷机台数设定点位
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2025/2/14 10:30
 */
@Getter
@AllArgsConstructor
public enum ChillerInfoCodeEnum implements BaseEnum<String>, IEnum<String> {

    /************************************** 台数设定点位 ***************************/

    BASE_LARGE_NUM("baseNumSet","baseNum", ChillerTypeSub.BASE, ChillerSize.LARGE, "key_9211360821ed401f9ba5d0725fb7b59a"),
    BASE_M1_NUM("baseM1NumSet", "baseM1Num", ChillerTypeSub.BASE, ChillerSize.M1, "key_558e871ec14842b0b379f6ccfb33d8c9"),
    BASE_SMALL_NUM("baseSNumSet","baseSNum", ChillerTypeSub.BASE, ChillerSize.SMALL, "key_6c9bea376ed94ccc8e372882329c7e61"),
    
    BASE_DUAL_LARGE_NUM("baseDualNumSet","baseDualNum", ChillerTypeSub.BASE_DUAL, ChillerSize.LARGE, "key_a6b984e719ea40368277285816a505c9"),
    BASE_DUAL_SMALL_NUM("baseDualSNumSet","baseDualSNum", ChillerTypeSub.BASE_DUAL, ChillerSize.SMALL, "key_d892625285d84ba28fea4d2a3f37de14"),

    ACCCCC_LARGE_NUM("acccccNumSet","acccccNum", ChillerTypeSub.ACCCCC, ChillerSize.LARGE, "key_d045fd89973d4f569c80388e9018803d"),
    ACCCCC_M1_NUM("acccccM1NumSet","acccccM1Num", ChillerTypeSub.ACCCCC, ChillerSize.M1, "key_ff595d19d49e4e8584fb8d420ae5c360"),
    ACCCCC_SMALL_NUM("acccccSNumSet","acccccSNum", ChillerTypeSub.ACCCCC, ChillerSize.SMALL, "key_d1500ee1c167470f9d436a655888d401"),
    
    ACCCCC_DUAL_LARGE_NUM("acccccDualNumSet","acccccDualNum", ChillerTypeSub.ACCCCC_DUAL, ChillerSize.LARGE, "key_62b94e936b0b42e183f21ae60aa4e209"),
    ACCCCC_DUAL_M1_NUM("acccccDualM1NumSet","acccccDualM1Num", ChillerTypeSub.ACCCCC_DUAL, ChillerSize.M1, "key_3c59fa8d80e14d3e916131100d0fb68b"),
    ACCCCC_DUAL_SMALL_NUM("acccccDualSNumSet","acccccDualSNum", ChillerTypeSub.ACCCCC_DUAL, ChillerSize.SMALL, "key_0e751919dfc94d25b4b3de53d74770fc"),

    ACCCSC_LARGE_NUM("acccscNumSet","acccscNum", ChillerTypeSub.ACCCSC, ChillerSize.LARGE, "key_a2792e46dffe4fd490b9baca0ae98217"),
    ACCCSC_M1_NUM("acccscM1NumSet","acccscM1Num", ChillerTypeSub.ACCCSC, ChillerSize.M1, "key_bc9ef37db5474caa912b99cd9b20351b"),
    ACCCSC_SMALL_NUM("acccscSNumSet","acccscSNum", ChillerTypeSub.ACCCSC, ChillerSize.SMALL, "key_d3f88a05cf8d42c49839a8eeff9a13ba"),
    
    ACCCSC_DUAL_LARGE_NUM("acccscDualNumSet","acccscDualNum", ChillerTypeSub.ACCCSC_DUAL, ChillerSize.LARGE, "key_d2bad3ce679c4196be38a1a4d5a7d887"),
    ACCCSC_DUAL_M1_NUM("acccscDualM1NumSet","acccscDualM1Num", ChillerTypeSub.ACCCSC_DUAL, ChillerSize.M1, "key_b234733562da4136a0b46eca71c6c539"),
    ACCCSC_DUAL_SMALL_NUM("acccscDualSNumSet","acccscDualSNum", ChillerTypeSub.ACCCSC_DUAL, ChillerSize.SMALL, "key_4b3b614f7997424b8e6d301ed0eea397"),

    ACCCWH_LARGE_NUM("acccwhNumSet","acccwhNum", ChillerTypeSub.ACCCWH, ChillerSize.LARGE, "key_40a603828066496d931c55287bfa2dd8"),
    ACCCWH_M1_NUM("acccwhM1NumSet","acccwhM1Num", ChillerTypeSub.ACCCWH, ChillerSize.M1, "key_f64f7cb17522419da2ce39af0c5383c8"),
    ACCCWH_SMALL_NUM("acccwhSNumSet","acccwhSNum", ChillerTypeSub.ACCCWH, ChillerSize.SMALL, "key_8733acd1b888475a9f4681690f4d2d73"),
    ACCCWH_DUAL_LARGE_NUM("acccwhDualNumSet","acccwhDualNum", ChillerTypeSub.ACCCWH_DUAL, ChillerSize.LARGE, "key_2e13942b4e2d4cf7859a5d1f4d7eeb83"),
    ACCCWH_DUAL_M1_NUM("acccwhDualM1NumSet","acccwhDualM1Num", ChillerTypeSub.ACCCWH_DUAL, ChillerSize.M1, "key_ad23fd296e744f0384fae9b6375bb1c7"),
    ACCCWH_DUAL_SMALL_NUM("acccwhDualSNumSet","acccwhDualSNum", ChillerTypeSub.ACCCWH_DUAL, ChillerSize.SMALL, "key_e7c103997bb84d74ba8d8dd8c5472a74"),

    ACCCGH_LARGE_NUM("acccghNumSet","acccghNum", ChillerTypeSub.ACCCGH, ChillerSize.LARGE, "key_0070a861afa6422eb96fbc2646377d1e"),
    ACCCGH_M1_NUM("acccghM1NumSet","acccghM1Num", ChillerTypeSub.ACCCGH, ChillerSize.M1, "key_57c344c51e514354b5ee31b2bb591eb0"),
    ACCCGH_SMALL_NUM("acccghSNumSet","acccghSNum", ChillerTypeSub.ACCCGH, ChillerSize.SMALL, "key_28fa84ea75ea4eb78c2e4b37b4ac18f2"),
    ACCCGH_DUAL_LARGE_NUM("acccghDualNumSet","acccghDualNum", ChillerTypeSub.ACCCGH_DUAL, ChillerSize.LARGE, "key_70fe3a191f1545b78830afac25515451"),
    ACCCGH_DUAL_M1_NUM("acccghDualM1NumSet","acccghDualM1Num", ChillerTypeSub.ACCCGH_DUAL, ChillerSize.M1, "key_450a859ac0bc4af2aa7929379e319972"),
    ACCCGH_DUAL_SMALL_NUM("acccghDualSNumSet","acccghDualSNum", ChillerTypeSub.ACCCGH_DUAL, ChillerSize.SMALL, "key_c4abc1e2c7cc469e951abd7661af9728"),

    ACAHC_LARGE_NUM("acahcNumSet","acahcNum", ChillerTypeSub.ACAHC, ChillerSize.LARGE, "key_9309c80f92a14425b82d73661f683108"),
    ACAHC_M1_NUM("acahcM1NumSet","acahcM1Num", ChillerTypeSub.ACAHC, ChillerSize.M1, "key_7a70b6c52ae743e68b5404e6c62bba20"),
    ACAHC_SMALL_NUM("acahcSNumSet","acahcSNum", ChillerTypeSub.ACAHC, ChillerSize.SMALL, "key_c8e7b9c2fd82483084a85a3f24ef819b"),

    ACCCAC_LARGE_NUM("acccacNumSet","acccacNum", ChillerTypeSub.ACCCAC, ChillerSize.LARGE, "key_450291af10e54d1d8280097a3eeb1659"),
    ACCCAC_M1_NUM("acccacM1NumSet","acccacM1Num", ChillerTypeSub.ACCCAC, ChillerSize.M1, "key_b8b7cc770f7147e385c8a3f3ce4bc3b0"),
    ACCCAC_SMALL_NUM("acccacSNumSet","acccacSNum", ChillerTypeSub.ACCCAC, ChillerSize.SMALL, "key_3c08bb1ce61c43e385f6943d379f7500"),

    PUMP_GROUP_LARGE_NUM("runNumSet","runNum", ChillerTypeSub.PUMP_GROUP_BASE, ChillerSize.LARGE, "key_996b531aa4634b71884efcbee29da65c"),
    PUMP_GROUP_M1_NUM("runM1NumSet","runM1Num", ChillerTypeSub.PUMP_GROUP_BASE, ChillerSize.M1, "key_4dcca0392b04443897bfc0e22222ba8c"),
    PUMP_GROUP_SMALL_NUM("runSNumSet","runSNum", ChillerTypeSub.PUMP_GROUP_BASE, ChillerSize.SMALL, "key_0533ef3b1f234d4b8e4d8281639e4587"),
    
    ACCHXB_LARGE_NUM("acchxbNumSet", "acchxbNum", ChillerTypeSub.ACCHXB, ChillerSize.LARGE,  "key_boiler_setting_large_machines"),
    ACCHXB_M1_NUM("acchxbM1NumSet", "acchxbM1Num", ChillerTypeSub.ACCHXB, ChillerSize.M1, "key_boiler_setting_m1_machines"),
    ACCHXB_SMALL_NUM("acchxbSNumSet", "acchxbSNum", ChillerTypeSub.ACCHXB, ChillerSize.SMALL, "key_boiler_setting_small_machines"),

    ACSCG_LARGE_NUM("acscgNumSet", "acscgNum", ChillerTypeSub.ACSCG, ChillerSize.LARGE,  "key_series_chiller_setting_large_units"),
    ACSCG_M1_NUM("acscgM1NumSet", "acscgM1Num", ChillerTypeSub.ACSCG, ChillerSize.M1, "key_series_chiller_setting_m1_units"),
    ACSCG_SMALL_NUM("acscgSNumSet", "acscgSNum", ChillerTypeSub.ACSCG, ChillerSize.SMALL, "key_series_chiller_setting_small_units"),


    ;



    private String infoCode;
    private String feedback;
    private ChillerTypeSub chillerType;
    private ChillerSize chillerSize;
    private String reasonPhrase;
    
    
    private static final Map<ChillerInfoCodeEnum, ChillerInfoCodeEnum> LARGE_TO_SMALL_INFO_CODE_MAP =
            new HashMap<ChillerInfoCodeEnum, ChillerInfoCodeEnum>() {{
        put(ChillerInfoCodeEnum.BASE_LARGE_NUM, ChillerInfoCodeEnum.BASE_SMALL_NUM);
        put(ChillerInfoCodeEnum.BASE_DUAL_LARGE_NUM, ChillerInfoCodeEnum.BASE_DUAL_SMALL_NUM);
        put(ChillerInfoCodeEnum.ACCCCC_LARGE_NUM, ChillerInfoCodeEnum.ACCCCC_SMALL_NUM);
        put(ChillerInfoCodeEnum.ACCCCC_DUAL_LARGE_NUM, ChillerInfoCodeEnum.ACCCCC_DUAL_SMALL_NUM);
        put(ChillerInfoCodeEnum.ACCCSC_LARGE_NUM, ChillerInfoCodeEnum.ACCCSC_SMALL_NUM);
        put(ChillerInfoCodeEnum.ACCCSC_DUAL_LARGE_NUM, ChillerInfoCodeEnum.ACCCSC_DUAL_SMALL_NUM);
        put(ChillerInfoCodeEnum.ACCCWH_LARGE_NUM, ChillerInfoCodeEnum.ACCCWH_SMALL_NUM);
        put(ChillerInfoCodeEnum.ACCCWH_DUAL_LARGE_NUM, ChillerInfoCodeEnum.ACCCWH_DUAL_SMALL_NUM);
        put(ChillerInfoCodeEnum.ACCCGH_LARGE_NUM, ChillerInfoCodeEnum.ACCCGH_SMALL_NUM);
        put(ChillerInfoCodeEnum.ACCCGH_DUAL_LARGE_NUM, ChillerInfoCodeEnum.ACCCGH_DUAL_SMALL_NUM);
        put(ChillerInfoCodeEnum.ACAHC_LARGE_NUM, ChillerInfoCodeEnum.ACAHC_SMALL_NUM);
        put(ChillerInfoCodeEnum.ACCCAC_LARGE_NUM, ChillerInfoCodeEnum.ACCCAC_SMALL_NUM);
        put(ChillerInfoCodeEnum.PUMP_GROUP_LARGE_NUM, ChillerInfoCodeEnum.PUMP_GROUP_SMALL_NUM);
        put(ChillerInfoCodeEnum.ACCHXB_LARGE_NUM, ChillerInfoCodeEnum.ACCHXB_SMALL_NUM);
        put(ChillerInfoCodeEnum.ACSCG_LARGE_NUM, ChillerInfoCodeEnum.ACSCG_SMALL_NUM);
    }};
    
    @Override
    public String value() {
        return infoCode;
    }

    @Override
    public String getValue() {
        return infoCode;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(reasonPhrase);
    }

    /**
     * 根据信息点编码，返回对应的枚举
     *
     * @param infoCode
     * @return
     */
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ChillerInfoCodeEnum analyze(String infoCode) {
        return BaseEnum.analyze(infoCode, ChillerInfoCodeEnum.class, false);
    }

    /**
     * 获取所有的信息点编码
     *
     * @return
     */
    public static Set<String> getAllChillerInfoCode() {
        Set<String> infoCodes = new HashSet<>();
        ChillerInfoCodeEnum[] infoCodeEnums = ChillerInfoCodeEnum.values();
        for (ChillerInfoCodeEnum infoCodeEnum : infoCodeEnums) {
            infoCodes.add(infoCodeEnum.getInfoCode());
        }
        return infoCodes;
    }

    /**
     * 获取所有双工况的信息点编码
     *
     * @return
     */
    public static Set<String> getAllDualChillerInfoCode() {
        Set<String> infoCodes = new HashSet<>();
        ChillerInfoCodeEnum[] infoCodeEnums = ChillerInfoCodeEnum.values();
        for (ChillerInfoCodeEnum infoCodeEnum : infoCodeEnums) {
            if (infoCodeEnum.getChillerType().belongDualObject()) {
                infoCodes.add(infoCodeEnum.getInfoCode());
            }
        }
        return infoCodes;
    }

    /**
     * 获取对应的信息点编码
     *
     * @param chillerTypSub
     * @param chillerSize
     * @return
     */
    public static String getChillerInfoCode(ChillerTypeSub chillerTypSub, ChillerSize chillerSize) {
        if (chillerTypSub == null || chillerSize == null) {
            return null;
        }
        ChillerInfoCodeEnum[] infoCodeEnums = ChillerInfoCodeEnum.values();
        for (ChillerInfoCodeEnum infoCodeEnum : infoCodeEnums) {
            if (chillerTypSub.equals(infoCodeEnum.getChillerType()) && chillerSize.equals(infoCodeEnum.getChillerSize())) {
                return infoCodeEnum.getInfoCode();
            }
        }
        return null;
    }

    /**
     * 获取对应的信息点编码
     *
     * @param chillerTypSub
     * @param chillerSize
     * @return
     */
    public static ChillerInfoCodeEnum getChiller(ChillerTypeSub chillerTypSub, ChillerSize chillerSize) {
        if (chillerTypSub == null || chillerSize == null) {
            return null;
        }
        ChillerInfoCodeEnum[] infoCodeEnums = ChillerInfoCodeEnum.values();
        for (ChillerInfoCodeEnum infoCodeEnum : infoCodeEnums) {
            if (chillerTypSub.equals(infoCodeEnum.getChillerType()) && chillerSize.equals(infoCodeEnum.getChillerSize())) {
                return infoCodeEnum;
            }
        }
        return null;
    }
    
    //获取大机信息点对应的小机信息点映射
    public static Map<ChillerInfoCodeEnum, ChillerInfoCodeEnum> getLargeToSmallInfoCodeMap() {
        return LARGE_TO_SMALL_INFO_CODE_MAP;
    }
    
    
}
