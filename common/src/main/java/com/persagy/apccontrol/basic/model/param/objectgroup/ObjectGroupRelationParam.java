package com.persagy.apccontrol.basic.model.param.objectgroup;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ObjectGroupRelationParam {


    /**
     * 对象id
     */
    @NotBlank(message = "{key_084485a601e5437688eeacf92ba08c45}")
    private String id;
    /**
     * 序列
     */
    private Integer sequence;
}
