package com.persagy.apccontrol.basic.model.vo.runanalysis;

import com.persagy.apccontrol.basic.model.enumtype.runanalysis.AnalysisTypeEnum;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 分析类型vo
 *
 * @author MuAnFu
 * @date 2025-08-13
 */
@Data
public class AnalysisTypeVO {

    /** 名称 */
    private String name;

    /** 值 */
    private String value;

    /** 顺序 */
    private Integer order;

    /**
     * 返回为列表
     *
     * @param isMonitorStatus 是否有监控状态
     * @return {@link List }<{@link Map }<{@link String }, {@link String }>>
     */
    public static List<AnalysisTypeVO> toList(Boolean isMonitorStatus) {
        List<AnalysisTypeVO> list = new LinkedList<>();
        for (AnalysisTypeEnum value : AnalysisTypeEnum.values()) {
            if (value == AnalysisTypeEnum.COLD_STORE_SIDE) {
                continue;
            }
            if (value == AnalysisTypeEnum.END_PUMP_GROUP) {
                continue;
            }
            if (isMonitorStatus) {
                list.add(of(value));
            }
        }
        return list;
    }

    public static AnalysisTypeVO of(AnalysisTypeEnum type) {
        AnalysisTypeVO vo = new AnalysisTypeVO();
        vo.setName(type.getName());
        vo.setValue(type.getValue());
        vo.setOrder(type.getOrder());
        return vo;
    }
}
