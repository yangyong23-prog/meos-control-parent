package com.persagy.apccontrol.basic.model.enumtype.runningLog;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum NowRunningLogCategory implements BaseEnum<String>, IEnum<String> {
    /**
     * 一级分类 产品定义的固定编码
     */
    //系统类型
    airConditioningColdSource("airConditioningColdSource", "ACAC", null, "key_dfbd48a8133047f8a6b51f2b5e34909f", 1,
            "#root.systemName", "#root.systemId", 1),
    
    xplantrunmonitor("xplantrunmonitor", "ACCC", null, "key_235e4049874342d1a209fa13c3711563", 1,
            "#root.systemName", "#root.systemId", 2),
    
    StatusAndControl_AC("StatusAndControl_AC", "ACCI", null, "key_9e432c33a6f3404cbc4295b2bdead343", 1,
            "#root.systemName", "#root.systemId", 3),
    
    airConditioningHeatSource("airConditioningHeatSource", "ACAH", null, "key_air_conditioning_heat_source", 1,
            "#root.systemName", "#root.systemId", 4),
    
    StatusAndControl_ACCH("StatusAndControl_ACCH", "ACCH", null, "key_2a7b0b8cf79743fb9409e9fcfa0f45fa", 1,
            "#root.systemName", "#root.systemId", 5),

    airConditioningHeatAndColdSource("airConditioningHeatAndColdSource", "ACAI", null, "key_0c76a5ab003b4931abb309e20aeefc8c", 1,
            "#root.systemName", "#root.systemId", 6),
    
    //非系统类型
    StatusAndControl_SPEVCD("StatusAndControl_SPEVCD", null, null, "key_56a25250df214dbbb7c1e62c8aae8801", 1, null, null, 5),
    
    lightxplantrunmonitor("lightxplantrunmonitor", null, null, "key_987c10b8393a415e91973b7a4cb6c93c", 1, null, null, 6),
    
    odlightxplantrunmonitor("odlightxplantrunmonitor", null, null, "key_081e6208d87e40ef9eeb1839024351f9", 1, null, null, 7),
    
    ventilatexplantrunmonitor("ventilatexplantrunmonitor", null, null, "key_03a55d56f20348f5ae2578117c489cb6", 1, null, null, 8),
    
    StatusAndControl_KEF("StatusAndControl_KEF", null, null, "key_a6ab383b51e34ecea42423395068ea47", 1, null, null, 9),
    
    space_Security("space_Security", null, null, "key_2b4bbb66befd48ae8b0c6df69ac7be89", 1, null, null, 10),
    
    wsSewageTreatment("wsSewageTreatment", null, null, "key_35cf9f6389744fce8946524f9baee565", 1, null, null, 11),

    //生活热水（系统类型）
    StatusAndControl_WS("StatusAndControl_WS", "WSDH", null, "key_statusandcontrol_ws", 1,
            "#root.systemName", "#root.systemId", 12),
    
    /**
     * 二级分类 二级分类编码为一级分类+clasee_code
     */
    
    //空调末端
    StatusAndControl_SPEVCD_ACATDH("StatusAndControl_SPEVCD_ACATDH", "ACATDH", "StatusAndControl_SPEVCD",
            "key_runninglog_statusandcontrol_spevcd_acatdh", 2,
            null, null, 0),
    StatusAndControl_SPEVCD_ACATAH("StatusAndControl_SPEVCD_ACATAH", "ACATAH", "StatusAndControl_SPEVCD", "key_61f2672c1aa74d079d9164368e09ea5c", 2,
            null, null, 1),
    StatusAndControl_SPEVCD_ACATFC("StatusAndControl_SPEVCD_ACATFC", "ACATFC", "StatusAndControl_SPEVCD", "key_4b93075451304904b7c9c4670ce2f1bd", 2,
            null, null, 2),
    StatusAndControl_SPEVCD_ACVRVI("StatusAndControl_SPEVCD_ACVRVI", "ACVRVI", "StatusAndControl_SPEVCD", "key_2da72e78d22542cea4a9d2c4cf27bf09",
            2, null, null, 3),
    StatusAndControl_SPEVCD_ACCCWP("StatusAndControl_SPEVCD_ACCCWP", "ACCCWP", "StatusAndControl_SPEVCD", "key_507f927f1add400b9d9aa85068ba3ab3", 2,
            null, null, 4),
    StatusAndControl_SPEVCD_ACATFA("StatusAndControl_SPEVCD_ACATFA", "ACATFA", "StatusAndControl_SPEVCD", "key_7ad6f8b08999498a9a107bff3b7d80fb", 2,
            null, null, 5),
    StatusAndControl_SPEVCD_ACATVA("StatusAndControl_SPEVCD_ACATVA", "ACATVA", "StatusAndControl_SPEVCD", "key_8068f28fc3b54897ac7b21e327143af3", 2,
            null, null, 6),
    StatusAndControl_SPEVCD_ACATFU("StatusAndControl_SPEVCD_ACATFU", "ACATFU", "StatusAndControl_SPEVCD", "key_128dcb016f7d444992e96dfc92acb8c4", 2,
            null, null, 7),
    StatusAndControl_SPEVCD_ACATRA("StatusAndControl_SPEVCD_ACATRA", "ACATRA", "StatusAndControl_SPEVCD", "key_017e2fbacc3049ae9aeffe22f63ca5e9", 2,
            null, null, 8),
    StatusAndControl_SPEVCD_ACVTAC("StatusAndControl_SPEVCD_ACVTAC", "ACVTAC", "StatusAndControl_SPEVCD", "key_5ea45db5a4264a77bad30d7cec8252b0", 2,
            null, null, 9),
    StatusAndControl_SPEVCD_ACVTSF("StatusAndControl_SPEVCD_ACVTSF", "ACVTSF", "StatusAndControl_SPEVCD", "key_e5beefd52e684925b7b1dc89f28a8ca8", 2,
            null, null, 10),
    StatusAndControl_SPEVCD_FFSCAF("StatusAndControl_SPEVCD_FFSCAF", "FFSCAF", "StatusAndControl_SPEVCD", "key_a5f73583fccd4853a7c060e126a03e2c"
            , 2, null, null, 11),
    StatusAndControl_SPEVCD_CFCSEW("StatusAndControl_SPEVCD_CFCSEW", "CFCSEW", "StatusAndControl_SPEVCD", "key_9cf9a46ef8f84f4d8dc6187e74c20a2c", 2,
            null, null, 12),
    StatusAndControl_SPEVCD_ACVVEJ("StatusAndControl_SPEVCD_ACVVEJ", "ACVVEJ", "StatusAndControl_SPEVCD", "key_fe3f10da6f7d496991ad8a970d95f513", 2
            , null, null, 13),
    StatusAndControl_SPEVCD_ACAHUF("StatusAndControl_SPEVCD_ACAHUF", "ACAHUF", "StatusAndControl_SPEVCD",
            "key_runninglog_statusandcontrol_spevcd_acahuf", 2
            , null, null, 14),
    StatusAndControl_SPEVCD_ACAHG("StatusAndControl_SPEVCD_ACAHG", "ACAHG", "StatusAndControl_SPEVCD",
            "key_runninglog_statusandcontrol_spevcd_acahg", 2
            , null, null, 16),
    StatusAndControl_SPEVCD_ACVVER("StatusAndControl_SPEVCD_ACVVER", "ACVVER", "StatusAndControl_SPEVCD",
            "key_runninglog_statusandcontrol_spevcd_acvver", 2
            , null, null, 17),
    StatusAndControl_SPEVCD_ACVVEA("StatusAndControl_SPEVCD_ACVVEA", "ACVVEA", "StatusAndControl_SPEVCD",
            "key_runninglog_statusandcontrol_spevcd_acvvea", 2
            , null, null, 18),
    StatusAndControl_SPEVCD_ACATHT("StatusAndControl_SPEVCD_ACATHT", "ACATHT", "StatusAndControl_SPEVCD",
            "key_runninglog_statusandcontrol_spevcd_acatht", 2
            , null, null, 19),
    StatusAndControl_SPEVCD_SPACE("StatusAndControl_SPEVCD_SPACE", "GeneralZone", "StatusAndControl_SPEVCD",
            "key_runninglog_statusandcontrol_spevcd_space", 2
            , null, null, 20),
    
    //室内照明
    lightxplantrunmonitor_LTLTCC("lightxplantrunmonitor_LTLTCC", "LTLTCC", "lightxplantrunmonitor", "key_10829ce5750c42f099af7dc60ca0205d", 2, null,
            null, 1),
    //室外照明
    odlightxplantrunmonitor_LTLTCC("odlightxplantrunmonitor_LTLTCC", "LTLTCC", "odlightxplantrunmonitor", "key_10829ce5750c42f099af7dc60ca0205d", 2,
            null, null, 2),
    
    //通风
    VENTILATION_FFSCAF("VENTILATION_FFSCAF", "FFSCAF", "ventilatexplantrunmonitor", "key_a5f73583fccd4853a7c060e126a03e2c", 2, null, null, 1),
    VENTILATION_FFSCMF("VENTILATION_FFSCMF", "FFSCMF", "ventilatexplantrunmonitor", "key_8cc120ed974043d8872de690b9ab6286", 2, null, null, 2),
    VENTILATION_ACVTSF("VENTILATION_ACVTSF", "ACVTSF", "ventilatexplantrunmonitor", "key_e5beefd52e684925b7b1dc89f28a8ca8", 2, null, null, 3),
    //新增通风下的
    VENTILATION_CFCSWN("VENTILATION_CFCSWN", "CFCSWN", "ventilatexplantrunmonitor", "key_a1b2c3d4e5f6478a0b1c2d3e4f50617", 2, null, null, 4),
    VENTILATION_CFCSEW("VENTILATION_CFCSEW", "CFCSEW", "ventilatexplantrunmonitor", "key_b2c3d4e5f6a7489b1c2d3e4f5061728", 2, null, null, 5),

    //厨房排油烟
    StatusAndControl_KEF_ACKLSF("StatusAndControl_KEF_ACKLSF", "ACKLSF", "StatusAndControl_KEF", "key_8c661f00552643dda5bd06e0316a4f77", 2, null,
            null, 1),
    StatusAndControl_KEF_ACATFU("StatusAndControl_KEF_ACATFU", "ACATFU", "StatusAndControl_KEF", "key_128dcb016f7d444992e96dfc92acb8c4", 2, null, null, 2),
    StatusAndControl_KEF_ACVTSF("StatusAndControl_KEF_ACVTSF", "ACVTSF", "StatusAndControl_KEF", "key_e5beefd52e684925b7b1dc89f28a8ca8", 2, null, null, 3),
    StatusAndControl_KEF_ACKLAP("StatusAndControl_KEF_ACKLAP", "ACKLAP", "StatusAndControl_KEF", "key_12287cb380244b1c9d3e8d98dbfad00d", 2, null,
            null, 4),
    
    //安防
    space_Security_CFCSDR("space_Security_CFCSDR", "CFCSDR", "space_Security", "key_2151d8478968432ea01ea604bd6b0444", 2, null, null, 1),
    space_Security_CFCSFR("space_Security_CFCSFR", "CFCSFR", "space_Security", "key_f93d26b5d605401cae19f0a227100a83", 2, null, null, 2),
    space_Security_SPEGFG("space_Security_SPEGFG", "SPEGFG", "space_Security", "key_84af2600d1e541fdbca865708839855b", 2, null, null, 3),
    space_Security_SPVSCM("space_Security_SPVSCM", "SPVSCM", "space_Security", "key_21d5db2bfdf44def97ca8817a618950d", 2, null, null, 4),
    space_Security_SPIACU("space_Security_SPIACU", "SPIACU", "space_Security", "key_0ac0ef612c8d479db681e2d5f99798bf", 2, null, null, 5),
    space_Security_SPEGCR("space_Security_SPEGCR", "SPEGCR", "space_Security", "key_996dc41549dc44b4a8387b5d81821841", 2, null, null, 6),
    
    //污水处理
    wsSewageTreatment_WSSTSP("wsSewageTreatment_WSSTSP", "WSSTSP", "wsSewageTreatment", "key_d22c65e67f49433090727796767285c4", 2, null, null, 1),

    //生活热水
    StatusAndControl_WS_WSDHHE("StatusAndControl_WS_WSDHHE", "WSDHHE", "StatusAndControl_WS", "key_statusandcontrol_ws_wsdhhe", 2, null, null, 1),
    StatusAndControl_WS_WSDHHP("StatusAndControl_WS_WSDHHP", "WSDHHP", "StatusAndControl_WS", "key_statusandcontrol_ws_wsdhhp", 2, null, null, 2),

    ;
    /**
     * 分类编码
     */
    private final String code;
    
    /**
     * 设备类
     */
    private final String classCode;
    
    /**
     * 上级分类编码
     */
    private final String parentCode;
    /**
     * 分类名称
     */
    private final String name;
    
    /**
     * 分类等级 一级/二级
     */
    private final int level;
    
    
    /**
     * 分类名称取数规则
     */
    private final String nameRule;
    /**
     * 分类编码取数规则
     */
    private final String codeRule;
    
    //顺序
    private final int order;
    
    //全量一级分类
    private static final List<NowRunningLogCategory> firstLevelCategoryList = new ArrayList<>();
    //一级分类+classCode对应的二级分类
    public static final Map<String, NowRunningLogCategory> parentCodeAndClassCodeMap = new HashMap<>();
    //创建 map 映射
    //全量编码映射
    private static final Map<String, NowRunningLogCategory> codeMap = new HashMap<>();
    
    //系统类型的分类 通过系统 calssCode 获取(空调冷源,冷源,冷热源,热源 )
    private static final Map<String, NowRunningLogCategory> systemCategoryByClassCodeMap = new HashMap<>();
    
    static {
        for (NowRunningLogCategory categoryEnum : values()) {

            if (categoryEnum.getLevel() == 1) {
                firstLevelCategoryList.add(categoryEnum);
            }
            codeMap.put(categoryEnum.getCode(), categoryEnum);
            
            //一级分类+classCode对应的二级分类
            parentCodeAndClassCodeMap.put(categoryEnum.getParentCode() + categoryEnum.getClassCode(), categoryEnum);
            
        }
        //初始化系统映射
        systemCategoryByClassCodeMap.put(airConditioningColdSource.getClassCode(), airConditioningColdSource);
        systemCategoryByClassCodeMap.put(xplantrunmonitor.getClassCode(), xplantrunmonitor);
        systemCategoryByClassCodeMap.put(StatusAndControl_AC.getClassCode(), StatusAndControl_AC);
        systemCategoryByClassCodeMap.put(StatusAndControl_ACCH.getClassCode(), StatusAndControl_ACCH);
        systemCategoryByClassCodeMap.put(airConditioningHeatSource.getClassCode(), airConditioningHeatSource);
        systemCategoryByClassCodeMap.put(airConditioningHeatAndColdSource.getClassCode(), airConditioningHeatAndColdSource);
        //生活热水系统映射
        systemCategoryByClassCodeMap.put(StatusAndControl_WS.getClassCode(), StatusAndControl_WS);
    }
    
    public static NowRunningLogCategory getEnumByCode(String code) {
        return codeMap.get(code);
    }
    
    //通过设备类类型 + 一级分类获取二级分类
    public static NowRunningLogCategory getSecondLevelCategoryByClassCode(String parentCode, String classCode) {
        return parentCodeAndClassCodeMap.get(parentCode + classCode);
    }
    
    //获取全量一级分类
    public static List<NowRunningLogCategory> getFirstLevelCategory() {
        return firstLevelCategoryList;
    }
    
    //获取 系统级别 全量一级分类
    public static Map<String, NowRunningLogCategory> getSystemCategoryByClassCode() {
        return systemCategoryByClassCodeMap;
    }
    
    /**
     * 判断是否是需要接入的系统
     *
     * @param category
     * @return true 需要接入
     */
    public static boolean newLoggingSystem(NowRunningLogCategory category) {
        List<NowRunningLogCategory> list = new ArrayList<>();
        list.add(NowRunningLogCategory.airConditioningColdSource);
        list.add(NowRunningLogCategory.StatusAndControl_AC);
        list.add(NowRunningLogCategory.StatusAndControl_SPEVCD);
        list.add(NowRunningLogCategory.lightxplantrunmonitor);
        list.add(NowRunningLogCategory.odlightxplantrunmonitor);
        list.add(NowRunningLogCategory.airConditioningHeatSource);
        list.add(NowRunningLogCategory.ventilatexplantrunmonitor);
        list.add(NowRunningLogCategory.StatusAndControl_KEF);
        list.add(NowRunningLogCategory.space_Security);
        list.add(NowRunningLogCategory.wsSewageTreatment);
        list.add(NowRunningLogCategory.airConditioningHeatAndColdSource);
        list.add(NowRunningLogCategory.StatusAndControl_WS);

        
        return list.contains(category);
    }
    
    
    @Override
    public String getValue() {
        return this.code;
    }
    
    @Override
    public String value() {
        return this.code;
    }
    
    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(this.name);
    }

    public String getName() {
        return LocaleMessageUtil.getMessage(this.name);
    }
}
