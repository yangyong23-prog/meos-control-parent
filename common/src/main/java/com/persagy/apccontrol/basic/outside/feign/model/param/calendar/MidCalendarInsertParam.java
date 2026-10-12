package com.persagy.apccontrol.basic.outside.feign.model.param.calendar;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 工作历新增参数
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@Data
public class MidCalendarInsertParam {

    /**
     * 对象ID
     */
    @NotBlank(message = "{key_db46294d82c44d7aa47bd996f27c6154}")
    private String objId;

    /**
     * 工作历名称
     */
    @NotBlank(message = "{key_f0784c142c3a4aa7ad6b912988fbfd97}")
    private String name;

    /**
     * 复制已有工作历ID
     */
    private String replicaCalId;

}
