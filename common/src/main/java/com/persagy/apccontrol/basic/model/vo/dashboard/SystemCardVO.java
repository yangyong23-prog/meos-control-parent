package com.persagy.apccontrol.basic.model.vo.dashboard;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import lombok.Data;

import java.util.Collection;

@Data
public class SystemCardVO extends DtpRwdObjectDTO {

    /**
     * 是否存在系统图
     */
    private Boolean isExistsSystemGraph;
    /**
     * 图形类型代码
     */
    private String graphTypeCode;
    /**
     * 图内对象id
     */
    private Collection<String> graphObjectIds;
}
