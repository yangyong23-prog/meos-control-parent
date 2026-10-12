package com.persagy.apccontrol.basic.model.param.objectgroup;

import com.persagy.apccontrol.basic.model.enumtype.objectgroup.ObjectGroupTypeEnum;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class QueryObjectGroupParam {

    /**
     * 编组类型
     */
    @NotNull(message = "{key_ab2eb518a7554bde9c65f938e873f443}")
    private ObjectGroupTypeEnum groupType;

}
