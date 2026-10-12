package com.persagy.apccontrol.basic.model.vo.objectgroup;

import com.persagy.apccontrol.basic.model.enumtype.objectgroup.ObjectGroupTypeEnum;
import lombok.Data;

import java.util.List;

@Data
public class ObjectGroupVO {

    /**
     * 编组名称
     */
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
     * 序列
     */
    private Integer sequence;
    /**
     * 编组类型
     */
    private ObjectGroupTypeEnum groupType;
    /**
     * 对象列表
     */
    private List<ObjectGroupObjectVO> objects;
}
