package com.persagy.apccontrol.basic.outside.feign.model.param.calendar;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 工作历删除参数
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@Data
public class MidCalendarDeleteParam {

    /**
     * 工作历ID
     */
    @NotBlank(message = "{key_e5b5d8e7d36c45abb215bf0f0aa77554}")
    private String workId;

}
