package com.persagy.apccontrol.basic.outside.feign.model.param.exceptionevent;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DangerInstanceTreeQueryParam {

    /**
     * 对象实例id集合
     */
    @NotNull
    private List<String> rwdInstanceIds;
    /**
     * 是否仅返回主动管理的风险事件实例，默认值：false(返回全部)
     */
    private Boolean followMark;

    public static DangerInstanceTreeQueryParam of(List<String> rwdInstanceIds, Boolean followMark) {
        return new DangerInstanceTreeQueryParam(rwdInstanceIds, followMark);
    }
}
