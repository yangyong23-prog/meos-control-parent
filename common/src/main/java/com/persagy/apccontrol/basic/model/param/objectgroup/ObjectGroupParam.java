package com.persagy.apccontrol.basic.model.param.objectgroup;

import com.persagy.apccontrol.basic.model.enumtype.objectgroup.ObjectGroupTypeEnum;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ObjectGroupParam {

    /**
     * 编组名称
     */
    @NotBlank(message = "{key_7ad7003b45ae4f79ae7c4627aaed315f}")
    private String groupName;
    /**
     * 编组主键id
     */
    private Long id;
    /**
     * 布局
     */
    private String layout;
    /**
     * 顺序
     */
    private Integer sequence;
    /**
     * 编组类型
     */
    @NotNull(message = "{key_ab2eb518a7554bde9c65f938e873f443}")
    private ObjectGroupTypeEnum groupType;
    /**
     * 对象列表
     */
    @NotNull(message = "{key_7d819ea7442b46248e4d7d5e86c85fa7}")
    private List<ObjectGroupRelationParam> objects;
}
