package com.persagy.apccontrol.basic.model.vo.infocode;

import lombok.Data;

import java.util.List;

/**
 * @ClassName 薛涛
 * @Author Jiangnan Cui
 * @Date 2024/6/28 18:29
 * @Version 1.0
 */
@Data
public class InfoCodeGroupVO {
    /**
     * 子分组
     */
    private List<InfoCodeGroupVO> groupChildren;
    /**
     * 分组编码
     */
    private String groupCode;
    /**
     * 分组名称
     */
    private String groupName;
    /**
     * 分组顺序
     */
    private Integer groupOrder;
    /**
     * 关联信息点配置
     */
    private List<InfoCodeConfigVO> relInfoCodeConfigs;

}
