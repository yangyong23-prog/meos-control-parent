package com.persagy.apccontrol.basic.model.entity;

import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * 信息点配置的扩展信息
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/4/29 15:30
 */
@Data
public class InfoCodeConfigExtendInfo {

    /**
     * 当前信息点所属类型，因为本方是全量信息点配置，在部分功能下，不需要全部展示
     */
    private Set<String> demandType;

    /**
     * 当前信息点需要保留的小数位，对应中台的scale
     */
    private Integer iotDecimal;

    /**
     * true-属于旧版控制信息点
     */
    private Boolean control;

    /**
     * 自定义功能点
     * 1-tag页，2-按钮
     */
    private String funcType;

    /**
     * 当前信息点，-1-仅后台逻辑使用，0-仅显示，1-显示及控制
     */
    private String view;

    /**
     * 当前信息点，-1 或 null 手自动下均展示, 0-手动下展示，1-自动下展示
     */
    private String force;

    /**
     * 额外信息点计算
     */
    private TriggerFormulaInfo triggerFormulaInfo;

    /**
     * 当前信息点，对应的强控信息点集合
     */
    private List<ForceInfoCodeInfo> forceInfoCodeInfos;

    /**
     * 是否属于聚合信息点，true-是
     */
    private Boolean merge;

    /**
     * 当前信息点，对应的聚合信息点
     */
    private List<String> mergeInfoCodes;

    /**
     * 重要等级
     */
    private String importanceLevel;

}
