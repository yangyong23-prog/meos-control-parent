package com.persagy.apccontrol.basic.service.runninglog;


import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO;
import com.persagy.apccontrol.basic.model.entity.RunningLog;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.*;

import static cn.hutool.poi.excel.sax.AttributeName.t;

/**
 * 模版配置
 */
public class RunningLogTemplateConfig {
    
    /**
     * 控制日志 过滤信息点值 ,这些信息点值都不记录
     */
    public static List<String> bdtpMeosFiltrationInfoCodeVal = Arrays.asList("release");
    
    
    
    /**
     * 模版六 孪生下发的自控信息点 多个 , 号分割
     */
    public static Map<String, List<String>> twinClassCodeInfoCodes = new HashMap<>();
    
    static {
        //空调冷源
        twinClassCodeInfoCodes.put("ACCCCP", Arrays.asList("equipSwitchSet", "equipFreqSet"));
        twinClassCodeInfoCodes.put("ACCPC", Arrays.asList("equipFreqSet", "equipSwitchSet", "runNumSet", "runM1NumSet"
                , "runSNumSet"));
        twinClassCodeInfoCodes.put("ACCCOP", Arrays.asList("equipFreqSet", "equipSwitchSet"));
//        twinClassCodeInfoCodes.put("ACCCOF", Arrays.asList("equipFreqSet", "equipSwitchSet"));和下面冷热源共用
        //冷热源
//        twinClassCodeInfoCodes.put("ACPPC", Collections.singletonList("freqSetCtrl")); 和下面空调冷热源共用
//        twinClassCodeInfoCodes.put("ACCHCH", Arrays.asList("equipFreqSet", "equipSwitchSet")); 和下面空调冷热源共用
//        twinClassCodeInfoCodes.put("ACCCOF", Arrays.asList("equipFreqSet", "equipSwitchSet")); 和下面空调冷热源共用
        //末端
//        twinClassCodeInfoCodes.put("ACATAH", Arrays.asList("equipSwitchSet", "tTargetSet", "cO2TargetSet"));
//        twinClassCodeInfoCodes.put("ACATFU", Arrays.asList("equipSwitchSet", "tTargetSet", "cO2TargetSet"));
//        twinClassCodeInfoCodes.put("ACATFC", Arrays.asList("equipSwitchSet", "tTargetSet", "cO2TargetSet"));
        //新增 变频多联机室内机自动日志
        twinClassCodeInfoCodes.put("ACVRVI", Arrays.asList("equipSwitchSet", "tTargetSet"));
        //通风
//        twinClassCodeInfoCodes.put("FFSCAF", Arrays.asList("equipSwitchSet", "srvAreaTargetTempSet","srvAreaTargetCOConcSet"));
//        twinClassCodeInfoCodes.put("FFSCMF", Arrays.asList("equipSwitchSet", "srvAreaTargetTempSet","srvAreaTargetCOConcSet"));
//        twinClassCodeInfoCodes.put("ACVTSF", Arrays.asList("equipSwitchSet", "tTargetSet","srvAreaTargetCOConcSet"));
        //厨房排油烟
        twinClassCodeInfoCodes.put("ACKLSF", Arrays.asList("equipSwitchSet"));
        twinClassCodeInfoCodes.put("ACATFU", Arrays.asList("equipSwitchSet"));
        twinClassCodeInfoCodes.put("ACVTSF", Arrays.asList("equipSwitchSet"));
        twinClassCodeInfoCodes.put("ACKLAP", Arrays.asList("equipSwitchSet"));
        //安防
        twinClassCodeInfoCodes.put("CFCSDR", Arrays.asList("accessDemandSet"));
        twinClassCodeInfoCodes.put("CFCSFR", Arrays.asList("accessDemandSet"));
        twinClassCodeInfoCodes.put("SPEGFG", Arrays.asList("accessDemandSet"));
        twinClassCodeInfoCodes.put("SPVSCM", Arrays.asList("armingSet"));

        //空调冷热源
        twinClassCodeInfoCodes.put("ACPPC", Arrays.asList("runNumSet", "runM1NumSet", "runSNumSet", "freqSetCtrl","equipSwitchSet"));
        twinClassCodeInfoCodes.put("ACCHCH", Arrays.asList("equipFreqSet", "equipSwitchSet"));
        twinClassCodeInfoCodes.put("ACCCOF", Arrays.asList( "equipFreqSet","equipSwitchSet"));

        //生活热水换热器
        twinClassCodeInfoCodes.put("WSDHHE", Arrays.asList("enableCtrl", "highVaporValveOpeningSet", "highVaporValveSwitchSet", "highValveOpeningSet", "highValveSwitchSet", "lowValveOpeningSet", "lowValveSwitchSet"));
        //生活热水水泵
        twinClassCodeInfoCodes.put("WSDHHP", Arrays.asList("equipSwitchSet", "equipFreqSet"));

    }
    
    // 控制指令的 公共模版规则
    static final String publicRule = "(#root.knowledgeInfoCode?.dataType == 'BOOLEAN' || #root.knowledgeInfoCode?.dataType == 'ENUM') " +
            "  ? #root.keyWord + ' ' + #root.objectName + ' ' + (#root.knowledgeInfoCode != null ? #root.knowledgeInfoCode.name : '') + '" +
            " ' + " +
            "    (#root.content.get('beforeValue') != null " +
            "      ? '" + LocaleMessageUtil.getMessage("key_runninglog_by") + "  ' + (#root.knowledgeInfoCode != null ? T(com.persagy" +
            ".apccontrol.basic.model.dto.knowledge" +
            ".DtpKnowledgeInfoCodeDTO)" +
            ".toDataSourceCodeAndName(#root.knowledgeInfoCode.dataSources).get(#root.content.get('beforeValue')) : '')  : '') + " +
            "  ' " + LocaleMessageUtil.getMessage("key_runninglog_change_to") + " ' + (#root.knowledgeInfoCode != null ? T(com.persagy" +
            ".apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO)" +
            ".toDataSourceCodeAndName(#root.knowledgeInfoCode.dataSources).get(#root.infoValue) : '')" +
            "  : #root.keyWord + ' ' + #root.objectName + ' ' + (#root.knowledgeInfoCode != null ? #root.knowledgeInfoCode.name : '') + " +
            "    (#root.content.get('beforeValue') != null ? ' " + LocaleMessageUtil.getMessage("key_runninglog_by") + " ' + #root" +
            ".content.get('beforeValue') + " +
            "(#root.knowledgeInfoCode != null ? #root.knowledgeInfoCode.unit : '') : '') + " +
            "  ' " + LocaleMessageUtil.getMessage("key_runninglog_change_to") + " '  +   #root.infoValue + (#root.knowledgeInfoCode != " +
            "null ? #root.knowledgeInfoCode.unit : '')";
    
    //判断是否有dataType
    static String dataTypeExist = "(#root.content.containsKey('dataType') && #root.content.get('dataType') != null)";
    
    /**
     * 操作内容模版
     * key 确定模版的匹配规则
     * val 是操作内容匹配规则
     */
    public static Map<String, String> bdtpMeosContentDesc = new LinkedHashMap<>();
    
    /**
     * twin服务模版
     */
    public static Map<String, String> twinContentDesc = new LinkedHashMap<>();
    
    /**
     * meos服务模版
     */
    public static Map<String, String> meosContentDesc = new LinkedHashMap<>();
    
    
    private static final ExpressionParser parser = new SpelExpressionParser();
    /**
     * 关键词模版
     */
    public static String keyWord =
            // 干预类型：返回具体原因
            "(#root.sourceType.value() == 'INTERVENTION') ? #root.sourceType.getReasonPhrase() : " +
                    // 时间表配置且有dataType：返回dataType 新增编辑删除
                    "(#root.sourceType.value() == 'SCHEDULE' && " + dataTypeExist + ") ? #root.content.get('dataType') : " +
                    // 修改截止时间 孪生控制 -> 本地化“调整”
                    "{'CHANGE_INTERVENTION_TIME','TWIN_COMMAND'}.contains(#root.sourceType.value()) ? '" + LocaleMessageUtil.getMessage(
                            "key_runninglog_adjust") + "' : " +
                    // 开关类：根据infoValue返回“开启”/“关闭”
                    "(#root.infoCode == 'equipSwitchSet') ? (#root.infoValue == '1' ? '" + LocaleMessageUtil.getMessage(
                            "key_runninglog_on") + "' : '" + LocaleMessageUtil.getMessage("key_runninglog_off") + "') : " +
                    // 未匹配项返回 调整
                    "'" + LocaleMessageUtil.getMessage("key_runninglog_adjust") + "'";

    static {
        
        
        /**
         * 模版六 关键词+操作对象+信息点名称+“变为”+信息点变化后的值
         * 示例：调整2#循环泵设备频率设定变为40Hz
         * 台数设定、冷冻水泵频率（对象是供冷冷冻水泵，信息点—运行频率workFreq）。
         */
        twinContentDesc.put(
                "(#root.sourceType.value() == 'TWIN_COMMAND') ? true : false",
                // 模版规则
                "#root.keyWord + #root.objectName + ' ' + #root.knowledgeInfoCode?.name + ' " + LocaleMessageUtil.getMessage(
                        "key_runninglog_change_to") + " ' + " +
                        "((#root.knowledgeInfoCode != null && (#root.knowledgeInfoCode.unit == null || #root.knowledgeInfoCode.unit == '')) ? " +
                        "T(com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO)" +
                        ".toDataSourceCodeAndName(#root.knowledgeInfoCode.dataSources).get(#root.infoValue) : " +
                        "#root.infoValue) + #root.knowledgeInfoCode?.unit"
        );
        
        
        /**
         * 模版八 关键词+操作对象+信息点名称+“的 干预结束时间 为”+最新截止时间
         * 示例：调整 1#空调机组 手自动状态设定 的 干预结束时间 为 2025.08.14 22:52
         */
        bdtpMeosContentDesc.put("(#root.sourceType.value() == 'CHANGE_INTERVENTION_TIME')?true:false",
                //模版规则
                "#root.keyWord + #root.objectName + ' ' + #root.knowledgeInfoCode?.name + ' " + LocaleMessageUtil.getMessage(
                        "key_runninglog_intervention_end_time_is") + " ' + " +
                        "T(java.time.LocalDateTime).parse(#root.content.get('endDateTime')).format(T(java.time.format" +
                        ".DateTimeFormatter).ofPattern('yyyy.M.d HH:mm'))");
        
        
        /**
         * 模版七 关键词+操作对象+信息点名称  关键词+“对于”+操作对象+信息点名称+“的”+干预
         * 示例：解除对于 2#循环泵 设备频率设定 的干预
         */
        bdtpMeosContentDesc.put("(#root.sourceType.value() == 'INTERVENTION')?true:false",
                //模版规则
                "#root.keyWord + '" + LocaleMessageUtil.getMessage("key_runninglog_for") + " ' + #root.objectName + ' ' + #root" +
                        ".knowledgeInfoCode?.name+ ' " + LocaleMessageUtil.getMessage("key_runninglog_intervention_suffix") + "'");
         
        /**
         * 匹配规则: （控制类消息），运行状态信息点参数设定
         * 模板三：关键词+操作对象
         * 开启1#冷站系统
         */
        bdtpMeosContentDesc.put("(#root.sourceType.value() == 'COMMAND' && !" + dataTypeExist + " && " +
                        "#root.infoCode == 'equipSwitchSet')?true:false",
                //模版规则
                "#root.keyWord + ' ' +  #root.objectName"
        );
        
        /**
         * 匹配规则: 控制类消息），来源为时间表，运行状态信息点参数设定。
         * 模板四：“根据时间表”+关键词+操作对象
         * 示例：根据时间表开启1#循环泵
         */
        bdtpMeosContentDesc.put("(#root.sourceType.value() == 'SCHEDULE' &&  !" + dataTypeExist + " &&  " +
                        "#root.infoCode == 'equipSwitchSet')?true:false",
                //模版规则
                "'" + LocaleMessageUtil.getMessage("key_runninglog_according_schedule") + "' + ' ' + #root.keyWord + ' ' + #root.objectName"
        );
        
        
        
        /**
         * 匹配规则: 除运行状态【信息点编码】信息点参数设定外的其他设定参数
         * 模板一：关键词+操作对象+信息点名称+“由”+信息点变化前的值+“变为”+信息点变化后的值
         * 示例：调整2#循环泵设备频率设定由45Hz变为40Hz
         * 如果是枚举类型 把值转换为名称
         */
        bdtpMeosContentDesc.put("( #root.sourceType.value() == 'COMMAND' && !" + dataTypeExist + " &&  " +
                        "#root.infoCode != 'equipSwitchSet')?true:false",
                //模版规则
                publicRule
        );
        

        
        /**
         * 匹配规则: （控制类消息），来源为时间表，除运行状态信息点参数设定外的其他设定参数。
         * 模板二：“根据时间表”+关键词+操作对象+信息点名称+“由”+信息点变化前的值+“变为”+信息点变化后的值
         * 示例：根据时间表调整2#循环泵设备频率设定由45Hz变为40Hz
         */
        bdtpMeosContentDesc.put("(#root.sourceType.value() == 'SCHEDULE' && !" + dataTypeExist + " &&  " +
                        "#root.infoCode != 'equipSwitchSet')?true:false",
                //模版规则
                "'" + LocaleMessageUtil.getMessage("key_runninglog_according_schedule") + " ' + (" + publicRule + ")"
        );
        
        
        /**
         * 模版五 时间表编辑规则 类型是时间表,存在编辑类型
         * 内容格式：关键词+操作对象+时间表日期区间+“固定时间策略”
         *    示例：新建南区冷热站2024年9月1日~2024年9月10日的固定时间策略
         */
        bdtpMeosContentDesc.put("(#root.sourceType.value() == 'SCHEDULE' && " + dataTypeExist + " )" +
                        "?true:false",
                //模版规则
                "#root.keyWord + ' ' + #root.objectName + ' ' + " +
                        "T(com.persagy.apccontrol.basic.utils.dateUtil.InternationalizationTimeUtils).timeTransition(#root.content.get" +
                        "('startDateTime')) + " +
                        " '~' + " +
                        "T(com.persagy.apccontrol.basic.utils.dateUtil.InternationalizationTimeUtils).timeTransition(#root.content.get" +
                        "('endDateTime')) + " +
                        "(#root.content.get('effectDateData') != null ? #root.content.get('effectDateData') : '')" +
                        "+' ' + #root.content.get('consoleTypeName') + '" + LocaleMessageUtil.getMessage(
                                "key_runninglog_fixed_time_policy") + "'");
        
        
        /**
         * 匹配规则: 服务内部规则 照明任务的开关任务
         * 模板九：关键词+"照明分类-任务名称"定时开关任务
         * 示例：新增"公共照明-周末开启夜景照明" 定时开关任务
         * 关键词：新建/编辑/删除
         */
        meosContentDesc.put("(#root.sourceType.value() == 'MEOS_LIGHTING_INTERVENTION_TASK')?true:false",
                //模版规则
                "#root.keyWord + '' +  #root.objectName + ' " + LocaleMessageUtil.getMessage("key_runninglog_timed_on_off_task") + "'"
        );
        
        
        /**
         * 匹配规则: 服务内部规则 末端任务的开关任务
         * 模板十：关键词+“临时需求名称”+临时需求
         * 示例：新增"周末万象汇一层加班开启空调" 临时需求
         * 关键词：新建/编辑/删除
         */
        meosContentDesc.put("(#root.sourceType.value() == 'MEOS_AIR_TERMINAL_INTERVENTION_TASK')?true:false",
                //模版规则
                "#root.keyWord + '' +  #root.objectName  + ' " + LocaleMessageUtil.getMessage("key_runninglog_temporary_demand") + "'"
        );
        
        
        DtpKnowledgeInfoCodeDTO.toDataSourceCodeAndName(null);
        
        
    }
    
    /**
     * spel 表达式解析
     * 返回字符串
     *
     * @param log
     */
    public static String spelMapping(Object log, String spelExpression) {
        EvaluationContext context = new StandardEvaluationContext(log);
        String r;
        try {
            Expression exp = parser.parseExpression(spelExpression);
            r = exp.getValue(context, String.class);
            //去掉null 字符
            if (r != null) {
                return r.replaceAll("null", "");
            }
        } catch (Exception e) {
            throw new RuntimeException("表达式解析失败: " + spelExpression + "\n" + e.getMessage());
        }
        return r;
    }
    
    /**
     * 规则解析 返回Boolean
     *
     * @param log
     * @param spelExpression
     * @return
     */
    public static Boolean spelMappingByBoolean(RunningLog log, String spelExpression) {
        EvaluationContext context = new StandardEvaluationContext(log);
        try {
            Expression exp = parser.parseExpression(spelExpression);
            return exp.getValue(context, Boolean.class);
        } catch (Exception e) {
            throw new RuntimeException("表达式解析失败: " + e.getMessage());
        }
    }
    
}
