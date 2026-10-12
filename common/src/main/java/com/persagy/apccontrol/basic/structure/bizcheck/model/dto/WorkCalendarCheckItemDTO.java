package com.persagy.apccontrol.basic.structure.bizcheck.model.dto;

import cn.hutool.core.lang.Range;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

/**
 * 工作日历当前值
 * 包含工作日历检查的各项实际值
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkCalendarCheckItemDTO {

    
    /**
     * 系统服务空间营业时间信息列表
     */
    private List<SystemBusinessHoursInfo> systemBusinessHours;
    
    /**
     * 系统服务空间营业时间信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemBusinessHoursInfo {
        /**
         * 系统ID
         */
        private String systemId;
        
        /**
         * 系统名称
         */
        private String systemName;
        
        /**
         * 空间营业时间信息列表
         */
        private List<Range<LocalTime>> spaceBusinessHours;
    }
}