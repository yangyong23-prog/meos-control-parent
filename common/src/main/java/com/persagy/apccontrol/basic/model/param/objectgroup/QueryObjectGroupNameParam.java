package com.persagy.apccontrol.basic.model.param.objectgroup;

import com.persagy.apccontrol.basic.model.enumtype.objectgroup.ObjectGroupTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

@Data
@Accessors(chain = true)
public class QueryObjectGroupNameParam {

    /**
     * 编组ID
     */
    private Long id;

    /**
     * 编组类型
     */
    @NotBlank(message = "{key_7ad7003b45ae4f79ae7c4627aaed315f}")
    private String groupName;

    /**
     * 编组类型
     */
    private ObjectGroupTypeEnum groupType;

}
