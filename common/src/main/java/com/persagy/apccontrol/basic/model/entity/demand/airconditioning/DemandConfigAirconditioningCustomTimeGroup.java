package com.persagy.apccontrol.basic.model.entity.demand.airconditioning;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 需求配置-空调需求配置-定制功能配置表
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DemandConfigAirconditioningCustomTimeGroup extends DemandConfigAirconditioningCustom {

    private static final long serialVersionUID = 1L;

    // 日期分组
    private LocalDate dateGroup;

    // 最大有效开始时间
    private LocalDateTime maxValidEndTime;
    
    /**
     * 数据分组对象
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompositeKey {
        String projectId;
        DemandInstanceTypeEnum demandInstanceType;
        String demandInstanceId;
        String relTemplateId;
        LocalDate targetDate;
        
    }
}