package com.persagy.apccontrol.basic.outside.feign.model.param.calendar;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Collection;

/**
 　　* @author weiyizhong
    * @Company yushu.biz
　　 * @date 2026/1/7 14:45
　　 * @description: 批量删除工作历
*/
@Data
public class MidCalendarDeleteBatchParam {

    /**
     * 工作历ID
     */
    @NotNull(message = "{key_e5b5d8e7d36c45abb215bf0f0aa77554}")
    private Collection<String> workIds;

}
