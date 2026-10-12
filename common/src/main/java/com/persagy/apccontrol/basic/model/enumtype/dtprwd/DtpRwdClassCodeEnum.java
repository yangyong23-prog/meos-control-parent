package com.persagy.apccontrol.basic.model.enumtype.dtprwd;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 物理世界设备类编码枚举
 *
 * @author ShaoHongbo
 * @since v5.0.0
 */
@Getter
@AllArgsConstructor
public enum DtpRwdClassCodeEnum implements BaseEnum<String>, IEnum<String> {

    // ------------------------- 项目/建筑/楼层/空间 -------------------------

    /**
     * 项目
     */
    PROJECT("project", "项目"),

    /**
     * 建筑
     */
    BUILDING("building", "建筑"),

    /**
     * 楼层编码
     */
    FLOOR("floor", "楼层"),

    /**
     * 空间
     */
    SPACE("GeneralZone", "空间"),

    /**
     * 空调控制分区
     */
    AC_SPACE("ACZone", "空调控制分区"),

    // ------------------------- 系统 -------------------------

    /**
     * 中央供冷系统
     */
    COLD_SYSTEM("ACCC", "中央供冷系统"),

    /**
     * 中央空调供冷全系统
     */
    ACAC_SYSTEM("ACAC", "中央空调供冷全系统"),
    /**
     * 中央空调供热全系统
     */
    ACAH_SYSTEM("ACAH", "中央空调供热全系统"),

    /**
     * 中央冷热源系统
     */
    COLD_HOT_SYSTEM("ACCI", "中央冷热源系统"),

    /**
     * 中央供热系统
     */
    HOT_SYSTEM("ACCH", "中央供热系统"),

    /**
     * 空调末端系统
     */
    ACAT_SYSTEM("ACAT", "空调末端系统"),
    /**
     * 分体空调系统
     */
    ACSA_SYSTEM("ACSA", "分体空调系统"),
    /**
     * 通风系统
     */
    ACVT_SYSTEM("ACVT", "通风系统"),

    /**
     * 中央冷热源系统
     */
    ACCI_SYSTEM("ACCI", "中央冷热源系统"),

    /**
     * 中央空调冷热源全系统
     */
    ACAI_SYSTEM("ACAI", "中央空调冷热源全系统"),

    /**
     * 高压配电系统
     */
    SEHD_SYSTEM("SEHD", "高压配电系统"),

    /**
     * 楼宇智能化系统
     */
    WEBA("WEBA", "楼宇智能化系统"),

    /**
     * 楼宇智能化系统
     */
    WEEM("WEEM", "能源管理系统"),

    /**
     * 环境监测系统
     */
    WEIE("WEIE", "环境监测系统"),

    /**
     * 照明监控系统
     */
    WELM("WELM", "照明监控系统"),

    TPAS("TPAS", "气源系统"),


    // ------------------------- 服务器 -------------------------

    /**
     * 服务器
     */
    WEICSV("WEICSV", "服务器"),

    // ------------------------- 网关 -------------------------

    /**
     * 网关
     */
    WENCGW("WENCGW", "网关"),

    // ------------------------- 控制器 -------------------------

    /**
     * DDC直接数字控制器
     */
    WEFCDD("WEFCDD", "DDC直接数字控制器"),

    /**
     * PLC可编程逻辑控制器
     */
    WEFCPL("WEFCPL", "PLC可编程逻辑控制器"),

    /**
     * 数据采集器
     */
    WEFCDC("WEFCDC", "数据采集器"),

    // ------------------------- 配电 -------------------------

    /**
     * 变压器
     */
    SETDTF("SETDTF", "变压器"),

    /**
     * 低压配电系统
     */
    SELD_SYSTEM("SELD", "低压配电系统"),
    FFSR_SYSTEM("FFSR", "自动喷水灭火系统"),
    SEDG_SYSTEM("SEDG", "自备发电机系统"),
    WSRW_SYSTEM("WSRW", "中水系统"),
    FFFM_SYSTEM("FFFM", "消防炮灭火系统"),
    FFFH_SYSTEM("FFFH", "消火栓灭火系统"),
    FFFS_SYSTEM("FFFS", "消防给水系统"),
    WSDW_SYSTEM("WSDW", "生活给水系统"),

    // ------------------------- 设备类控制组 -------------------------

    /**
     * 冷冻水泵控制组
     */
    ACCPC_GROUP("ACCPC", "冷冻水泵控制组"),

    /**
     * 冷却水泵控制组
     */
    ACOPC_GROUP("ACOPC", "冷却水泵控制组"),

    /**
     * 空气源热泵控制组
     */
    ACAHC_GROUP("ACAHC", "空气源热泵控制组"),

    /**
     * 供热水泵控制组
     */
    ACHPC_GROUP("ACHPC", "供热水泵控制组"),

    /**
     * 冷热共用水泵控制组
     */
    ACPPC_GROUP("ACPPC", "冷热共用水泵控制组"),

    /**
     * 离心式冷水机组控制组
     */
    ACCCC_GROUP("ACCCC", "离心式冷水机组控制组"),

    /**
     * 螺杆式冷水机组控制组
     */
    ACSCC_GROUP("ACSCC", "螺杆式冷水机组控制组"),

    /**
     * 吸收式冷水机组控制组
     */
    ACCAC_GROUP("ACCAC", "吸收式冷水机组控制组"),

    /**
     * 蓄冷热槽控制组
     */
    ACCST_GROUP("ACCST", "蓄冷热槽控制组"),

    /**
     * 冷却塔控制组
     */
    ACOTC_GROUP("ACOTC", "蓄冷热槽控制组"),
    /**
     * 供冷乙二醇泵控制组
     */
    ACCGP_GROUP("ACCGP", "蓄冷热槽控制组"),

    ACOTU_GROUP("ACOTU", "冷却塔控制单元"),


    /**
     * 串联冷机组
     */
    ACSCG_GROUP("ACSCG", "串联冷机组"),

    /**
     * 空调机组控制组
     *
     * @since v7.260502.0
     */
    ACAHG_GROUP("ACAHG", "空调机组控制组"),

    // ------------------------- 冷水机组 -------------------------

    /**
     * 离心式冷水机组
     */
    ACCCCC("ACCCCC", "离心式冷水机组"),

    /**
     * 螺杆式冷水机组
     */
    ACCCSC("ACCCSC", "螺杆式冷水机组"),

    /**
     * 活塞式冷水机组
     */
    ACCCPC("ACCCPC", "活塞式冷水机组"),

    /**
     * 吸收式冷水机组
     */
    ACCCAC("ACCCAC", "吸收式冷水机组"),

    // ------------------------- 冷却塔 -------------------------

    /**
     * 冷却塔
     */
    ACCCOT("ACCCOT", "冷却塔"),

    /**
     * 冷却塔风机
     */
    ACCCOF("ACCCOF", "冷却塔风机"),

    // ------------------------- 水泵 -------------------------

    /**
     * 供冷冷冻水泵
     */
    ACCCCP("ACCCCP", "供冷冷冻水泵"),

    /**
     * 冷热水循环水泵
     */
    ACCHCH("ACCHCH", "冷热水循环水泵"),

    /**
     * 供冷冷却水泵
     */
    ACCCOP("ACCCOP", "供冷冷却水泵"),

    /**
     * 供热泵
     */
    ACCHHP("ACCHHP", "供热水泵"),

    /**
     * 地源热泵
     */
    ACCCGH("ACCCGH", "地源热泵"),

    /**
     * 水源热泵
     */
    ACCCWH("ACCCWH", "水源热泵"),

    /**
     * 生活热水换热器
     */
    WSDHHE("WSDHHE", "生活热水换热器"),

    /**
     * 生活热水水泵
     */
    WSDHHP("WSDHHP", "生活热水水泵"),

    // ------------------------- 表 -------------------------

    /**
     * 电能表
     */
    WESSEE("WESSEE", "电能表"),

    /**
     * 水表
     */
    WESSWM("WESSWM", "水表"),

    /**
     * 锅炉
     */
    ACCHGB("ACCHGB", "锅炉"),

    ACCCGP("ACCCGP", "供冷乙二醇泵"),
    ACCCTP("ACCCTP", "蓄冷热槽循环泵"),
    ACCCVD("ACCCVD", "真空脱气机"),
    ACCCCD("ACCCCD", "水加药装置"),
    ACCCTU("ACCCTU", "全程水处理仪"),
    ACCCST("ACCCST", "蓄冷热槽"),
    ACCCWT("ACCCWT", "补水箱"),
    ACCCOC("ACCCOC", "在线清洗装置"),
    ACGEST("ACGEST", "软化水箱"),
    ACCCGT("ACCCGT", "乙二醇补液箱"),
    ACGEET("ACGEET", "膨胀水箱"),
    ACGECF("ACGECF", "定压补水装置"),
    ACGEWS("ACGEWS", "软水器"),
    ACGEFP("ACGEFP", "补水泵"),
    ACGEPT("ACGEPT", "稳压罐"),
    ACGEHE("ACGEHE", "换热器"),
    ACVVNO("ACVVNO", "非电动通断水阀"),
    ACVVEO("ACVVEO", "电动通断水阀"),
    ACVVNJ("ACVVNJ", "非电动调节水阀"),
    ACVVEJ("ACVVEJ", "电动调节水阀"),

    /**
     * 电动调节风阀
     *
     * @since v7.260502.0
     */
    ACVVER("ACVVER", "电动调节风阀"),

    /**
     * 电动通断风阀
     *
     * @since v7.260502.0
     */
    ACVVEA("ACVVEA", "电动通断风阀"),

    ACVVSB("ACVVSB", "静态平衡阀"),
    ACVVDB("ACVVDB", "动态平衡阀"),
    ACVVWC("ACVVWC", "水管止回阀"),
    ACVVRV("ACVVRV", "减压阀"),
    ACVVSV("ACVVSV", "安全阀"),
    ACVVEV("ACVVEV", "排气阀"),
    ACVVTW("ACVVTW", "三通阀"),
    WESSFL("WESSFL", "流量传感器"),
    WESSHS("WESSHS", "热量传感器"),
    WESSPM("WESSPM", "水压表"),
    FFGCFS("FFGCFS", "流量开关"),
    ACATDH("ACATDH", "热转轮除湿机组"),

    /**
     * 风管式再热装置
     *
     * @since v7.260502.0
     */
    ACATHT("ACATHT", "风管式再热装置"),

    // ------------------------- 分水器 -------------------------

    /**
     * 空气源热泵
     */
    ACCCAH("ACCCAH", "空气源热泵"),

    /**
     * 分水器
     */
    ACGEDB("ACGEDB", "分水器"),

    // ------------------------- 集水器 -------------------------

    /**
     * 集水器
     */
    ACGECL("ACGECL", "集水器"),

    // ------------------------- 照明回路 -------------------------

    /**
     * 照明回路
     */
    LIGHTING_CIRCUIT("LTLTCC", "照明回路"),

    /**
     * 照明控制模块
     */
    LIGHTING_CONTROLLER("LTLTCU", "照明控制模块"),

    /**
     * 照明位移传感器
     */
    WESSID("WESSID", "照明位移传感器"),

    // ------------------------- 低压末端配电柜 -------------------------

    /**
     * 低压末端配电柜
     */
    DISTRIBUTION_CABINET("SETDLT", "低压末端配电柜"),

    // ------------------------- 风机 -------------------------

    /**
     * 通风风机
     */
    SUPPLY_FAN("ACVTSF", "通风风机"),

    /**
     * 排风兼机械排烟风机
     */

    AIR_AND_MECHANICAL_SMOKE_EXHAUST_FAN("FFSCAF", "排风兼机械排烟风机"),

    /**
     * 送风兼消防补风机
     */
    AIR_SUPPLY_AND_MAKEUP_FAN("FFSCMF", "送风兼消防补风机"),
    /**
     * 排油烟风机
     */
    ACKLSF("ACKLSF", "排油烟风机"),

    // ------------------------- 传感器 -------------------------

    /**
     * 温度传感器
     */
    WESSTP("WESSTP", "温度传感器"),

    /**
     * 湿度传感器
     */
    WESSRH("WESSRH", "湿度传感器"),

    /**
     * 温湿度传感器
     */
    WESSTH("WESSTH", "温湿度传感器"),

    /**
     * 组合式传感器
     */
    WESSCB("WESSCB", "组合式传感器"),

    /**
     * 浓度传感器
     */
    WESSCT("WESSCT", "浓度传感器"),

    /**
     * 照度传感器
     */
    WESSIL("WESSIL", "照度传感器"),
    /**
     * 液位压差传感器
     */
    WESSFD("WESSFD", "液体压差传感器"),

    /**
     * CO2和PM2.5二合一传感器
     */
    WESSCP("WESSCP", "CO2和PM2.5二合一传感器"),

    /**
     * 位移传感器
     */
    WESSDP("WESSDP", "位移传感器"),
    /**
     * CO传感器
     */
    WESSCO("WESSCO", "CO传感器"),

    /**
     * 室内定位传感器
     */
    WESSIP("WESSIP", "室内定位传感器"),

    /**
     * 压力传感器
     */
    WESSPE("WESSPE", "压力传感器"),

    /**
     * 人流量传感器
     */
    WESSPF("WESSPF", "人流量传感器"),

    /**
     * 声音传感器
     */
    WESSSD("WESSSD", "声音传感器"),

    /**
     * 速度传感器
     */
    WESSSP("WESSSP", "速度传感器"),

    /**
     * 空气质量三合一传感器
     */
    WESSTO("WESSTO", "空气质量三合一传感器"),

    /**
     * 图像传感器
     */
    WESSVD("WESSVD", "图像传感器"),

    /**
     * 水浸传感器
     */
    WESSWL("WESSWL", "水浸传感器"),

    /**
     * 液位传感器
     */
    WESSWV("WESSWV", "液位传感器"),

    // ------------------------- 空调 -------------------------

    /**
     * 空调
     */
    ACATAH("ACATAH", "空调机组"),

    // ------------------------- 新风机组 -------------------------

    /**
     * 新风机组
     */
    ACATFU("ACATFU", "新风机组"),


    // ------------------------- 变风量箱 -------------------------

    /**
     * 变风量箱
     */
    ACATVA("ACATVA", "变风量箱"),

    // ------------------------- 新风换气机 -------------------------

    /**
     * 新风换气机
     */
    ACATRA("ACATRA", "新风换气机"),

    // ------------------------- 水冷柜机 -------------------------

    /**
     * 水冷柜机
     */
    ACCCWP("ACCCWP", "水冷柜机"),

    // ------------------------- 地板空调器 -------------------------

    /**
     * 地板空调器
     */
    ACATFA("ACATFA", "地板空调器"),

    // ------------------------- 多联机室内机 -------------------------

    /**
     * 多联机室内机
     */
    ACVRVI("ACVRVI", "多联机室内机"),

    // ------------------------- 风幕机 -------------------------

    /**
     * 风幕机
     */
    ACVTAC("ACVTAC", "风幕机"),

    // ------------------------- 空调末端 ------------------------

    CFCSEW("CFCSEW", "排烟窗"),

    // ------------------------- 风机盘管 -------------------------

    /**
     * 风机盘管
     */
    ACATFC("ACATFC", "风机盘管"),

    CFCSWN("CFCSWN", "普通窗"),


    ACVTSF("ACVTSF", "通风风机"),

    FFSCAF("FFSCAF", "排风兼机械排烟风机"),

    FFSCMF("FFSCMF", "送风兼消防补风机"),

    // ------------------------- 水管道 -------------------------

    /**
     * 冷冻水供水管
     */
    ACPPCS("ACPPCS", "冷冻水供水管"),

    /**
     * 冷冻水回水管
     */
    ACPPCR("ACPPCR", "冷冻水回水管"),

    /**
     * 冷冻水旁通管
     */
    ACPPCB("ACPPCB", "冷冻水旁通管"),

    /**
     * 冷热水供水管
     */
    ACPPAS("ACPPAS", "冷热水供水管"),

    /**
     * 冷热水回水管
     */
    ACPPAR("ACPPAR", "冷热水回水管"),

    /**
     * 冷热水旁通管
     */
    ACPPAB("ACPPAB", "冷热水旁通管"),

    // ------------------------- 风管道 -------------------------

    /**
     * 回风兼排风管
     */
    ACPPRE("ACPPRE", "回风兼排风管"),

    /**
     * 空调回风管
     */
    ACPPRA("ACPPRA", "空调回风管"),

    /**
     * 空调排风管
     */
    ACPPEA("ACPPEA", "空调排风管"),

    /**
     * 空调送风管
     */
    ACPPSA("ACPPSA", "空调送风管"),

    /**
     * 新风送风管
     */
    ACPPFS("ACPPFS", "新风送风管"),

    /**
     * 分集水器旁通管
     */
    ACPPWC("ACPPWC", "分集水器旁通管"),

    // ------------------------- 变频器 -------------------------

    /**
     * 变频器
     */
    SETDRC("SETDRC", "变频器"),

    // ------------------------- 压缩机 -------------------------

    /**
     * 空气压缩机
     */
    TPASAC("TPASAC", "空气压缩机"),

    TPASHR("TPASHR", "余热回收装置"),
    TPASCT("TPASCT", "压缩空气储罐"),
    TPASCF("TPASCF", "压缩空气过滤器"),
    TPASRD("TPASRD", "冷冻式干燥机"),
    TPASDD("TPASDD", "吸附式干燥机"),
    TPASPP("TPASPP", "热回收冷却循环水泵"),
    TPPPAS("TPPPAS", "普通压缩空气管"),
    IMIMIM("IMIMIM", "生产主工艺设备"),
    FFSRAC("FFSRAC", "空压机"),

    /**
     * ==========强电==========
     **/
    SETDDD("SETDDD", "低压配电抽屉"),

    /**
     * 集水坑 隔油池 餐饮除油成套设备
     **/
    WSSTCP("WSSTCP", "集水坑"),

    WSSTSP("WSSTSP", "潜污泵"),

    WSDWSU("WSDWSU", "生活给水供水机组"),

    WSSTLU("WSSTLU", "污废水一体式提升机组"),

    WSSTOS("WSSTOS", "隔油池"),

    WSSTOR("WSSTOR", "餐饮除油成套设备"),
    WSDWPP("WSDWPP", "生活给水水泵"),
    WSDWWT("WSDWWT", "生活给水储水箱"),
    WSRWPP("WSRWPP", "中水水泵"),


    // ---------------------空间安防---------------------
    SPIACU("SPIACU", "入侵报警防区控制器"),
    SPVSCM("SPVSCM", "监控摄像头"),
    CFCSDR("CFCSDR", "普通门"),
    CFCSFR("CFCSFR", "防火门"),
    SPEGFG("SPEGFG", "速通门"),
    CFCSED("CFCSED", "电梯门"),
    WEGMCM("WEGMCM", "车库摄像头"),
    SPVSVE("SPVSVE", "视频编码器"),
    SPVSDR("SPVSDR", "硬盘录像机"),
    SPIAGD("SPIAGD", "玻璃破碎探测器"),
    SPIAID("SPIAID", "红外探测器"),
    SPIAMA("SPIAMA", "入侵报警手动报警按钮"),
    SPEGCU("SPEGCU", "门禁控制器"),
    SPEGCR("SPEGCR", "门禁识别器"),


    // ---------------------机器人---------------------
    PDPDRB("PDPDRB", "搬运机器人"),
    PDPDBX("PDPDBX", "搬运箱"),

    // ---------------------客流---------------------
    CFDRG("CFDRG", "门组"),
    WESEPC("WESEPC", "客流统计摄像头"),


    // ---------------------电梯---------------------
    ELELEL("ELELEL", "直梯"),

    ELELES("ELELES", "扶梯"),


    // ---------------------停车场---------------------
    CFPKPS("CFPKPS", "停车位"),
    SEGECP("SEGECP", "充电桩"),
    WEGMGT("WEGMGT", "道闸"),


    WSDHWT("WSDHWT", "生活热水储水箱"),

    // ---------------------消防---------------------
    FFFSTP("FFFSTP", "消防水池"),

    FFFSWT("FFFSWT", "消防水箱"),

    // ---------------------厨房油烟机---------------------
    ACKLAP("ACKLAP", "油烟净化器"),
    WSGECN("WSGECN", "市政接驳点"),

    // ---------------------消防电------------------------------------------------

    //  ---------------------消防电系统---------------------
    FFFA("FFFA", "火灾报警系统"),
    FFEA("FFEA", "电气火灾监控系统"),
    FFGA("FFGA", "可燃气体探测报警系统"),
    FFPM("FFPM", "消防设备电源监控系统"),
    FFCU("FFCU", "厨房动火离人系统"),
    FFSC("FFSC", "建筑防烟排烟系统"),
    FFEE("FFEE", "消防应急照明和疏散指示系统"),
    FFEB("FFEB", "消防应急广播系统"),
    FFFR("FFFR", "防火分隔系统"),
    FFGE("FFGE", "气体灭火系统"),
    FFRP("FFRP", "余压监控系统"),

    //  ---------------------火灾报警系统设备---------------------
    FFFACU("FFFACU", "火灾报警控制器"),
    FFFASE("FFFASE", "火灾探测器"),
    FFFAMA("FFFAMA", "手动火灾报警按钮"),
    FFFATE("FFFATE", "消防专用电话分机"),
    FFFACE("FFFACE", "消防专用电话总机"),
    FFFAHA("FFFAHA", "气体灭火系统"),
    //  ---------------------电气火灾监控系统 设备---------------------
    FFEAFM("FFEAFM", "电气火灾监控器"),
    FFEAFD("FFEAFD", "电气火灾探测器"),
    //  ---------------------可燃气体探测报警系统 设备---------------------
    FFGACR("FFGACR", "可燃气体探测报警控制器"),
    FFGADT("FFGADT", "可燃气体探测器"),
    //  ---------------------消防设备电源监控系统 设备---------------------
    FFPMSM("FFPMSM", "消防设备电源状态监控器"),
    FFPMSR("FFPMSR", "消防设备电源传感器"),
    //  ---------------------厨房动火离人系统 设备---------------------
    FFCUCR("FFCUCR", "厨房动火离人报警控制器"),
    FFCUDT("FFCUDT", "厨房动火离人报警探测器"),
    //  ---------------------建筑防烟排烟系统 设备---------------------
    FFSCSF("FFSCSF", "机械加压送风机"),
    FFSCEF("FFSCEF", "机械排烟风机"),
    // 已存在 FFSCAF("FFSCAF", "排风兼机械排烟风机"),
    FFSCDF("FFSCDF", "消防补风机"),
    //  ---------------------消防应急照明和疏散指示系统 设备---------------------
    FFEESL("FFEESL", "消防应急标志灯具"),
    FFEELG("FFEELG", "消防应急照明灯具"),
    FFEECT("FFEECT", "应急照明和疏散指示回路"),
    FFEECR("FFEECR", "应急照明控制器"),
    //  ---------------------消防应急广播系统 设备---------------------
    FFEBHT("FFEBHT", "消防应急广播主机"),
    FFEBCR("FFEBCR", "消防应急广播分区控制器"),
    //  ---------------------防火分隔系统 设备---------------------
    CFCAHW("CFCAHW", "挡烟垂壁"),
    CFCSFS("CFCSFS", "防火卷帘"),
    CFCSFW("CFCSFW", "防火窗"),
    // 已存在 CFCSFR("CFCSFR", "防火门"),
    //  ---------------------气体灭火系统 设备---------------------
    FFGECR("FFGECR", "气体灭火控制器"),
    //  ---------------------余压监控系统 设备---------------------
    FFRPPM("FFRPPM", "余压监控器"),
    FFRPCL("FFRPCL", "余压控制器"),
    FFRPDT("FFRPDT", "余压探测器"),
    // ---------------------气象检测站---------------------
    WESEMS("WESEMS", "气象监测站"),

    //  ---------------------卫生间---------------------
    CFIDBP("CFIDBP", "卫生间隔间"),
    WESSBP("WESSBP", "厕位感应器"),
    // ---------------------光伏---------------------
    SEEA("SEEA", "清洁能源辅助供电系统"),
    SEDGGI("SEDGGI", "并网逆变器"),
    SEDGBP("SEDGBP", "防逆流控制柜"),
    SEDGCB("SEDGCB", "汇流箱"),
    SEDGSP("SEDGSP", "太阳能电池板"),
    SEDGSC("SEDGSC", "同期柜"),
    SEDGGC("SEDGGC", "并网柜"),
    SETDIV("SETDIV", "逆变器"),
    
    
    ACSSVG("ACSSVG", "季节切换阀控制组"),
    
    ;


    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public String getReasonPhrase() {
        return reasonPhrase;
    }

    @Override
    public String toString() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static DtpRwdClassCodeEnum analyze(String value) {
        return BaseEnum.analyze(value, DtpRwdClassCodeEnum.class);
    }

    /**
     * 空调供冷/供热/冷热源六类系统类码列表
     * <p>
     * 旧版：中央供冷系统 ACCC、中央供热系统 ACCH、中央冷热源系统 ACCI；
     * 新版：中央空调供冷全系统 ACAC、中央空调供热全系统 ACAH、中央空调冷热源全系统 ACAI。
     *
     * @return 六类空调系统类码列表
     * @since v7.260902.0
     */
    public static List<String> listAirConditionSystemClassCodes() {
        return Arrays.asList(COLD_SYSTEM.value(), HOT_SYSTEM.value(), COLD_HOT_SYSTEM.value(),
                ACAC_SYSTEM.value(), ACAH_SYSTEM.value(), ACAI_SYSTEM.value());
    }
}
