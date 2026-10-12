package com.persagy.apccontrol.basic.outside.feign.model.param.calendar;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.Collection;

/**
 * 工作历规则删除参数
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@Data
public class MidCalendarRuleDeleteParam {

    /**
     * 工作历ID
     */
    @NotBlank(message = "{key_e5b5d8e7d36c45abb215bf0f0aa77554}")
    private String workId;

    /**
     * 规格信息
     */
    @NotEmpty(message = "{key_0e6c39993a774ea8b26e2112a1166615}")
    @Valid
    private Collection<RuleInfo> ruleInfo;


    /**
     * 规格信息
     *
     * @author JiYinchuan
     * @since v5.7.0
     */
    @Data
    @Accessors(chain = true)
    public static class RuleInfo {

        /**
         * 规则ID
         */
        @NotBlank(message = "{key_70f2291720bf41e2991c5f8dd4bb7dca}")
        private String ruleId;

    }
}
