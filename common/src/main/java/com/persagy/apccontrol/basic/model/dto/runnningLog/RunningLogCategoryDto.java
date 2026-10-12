package com.persagy.apccontrol.basic.model.dto.runnningLog;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class RunningLogCategoryDto implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 分类编码
     */
    private String code;

    /**
     * 上级分类编码
     */
    private String parentCode;
    /**
     * 分类名称
     */
    private String name;
    
    //顺序
    private int order;

    /**
     * 分类等级
     */
    private int level;

    private List<RunningLogCategoryDto> childCategoryList;

}
