package com.persagy.apccontrol.basic.model.vo.log;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 运行日值班首页日志
 */
@Data
public class RunningLogHomePageVO {
    
    
    /**
     * 一级分类名称
     */
    private String firstLevelCategoryName;
    
    /**
     * 描述信息
     */
    private String contentDesc;
    
    /**
     * 操作人
     */
    private String createUser;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    
}
