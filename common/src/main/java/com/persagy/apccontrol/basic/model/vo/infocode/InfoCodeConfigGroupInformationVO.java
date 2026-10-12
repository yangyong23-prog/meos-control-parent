package com.persagy.apccontrol.basic.model.vo.infocode;

import lombok.Data;

/**
 * @ClassName 薛涛
 * @Description 平铺的信息点加入group
 * @Author Jiangnan Cui
 * @Date 2024/6/28 18:30
 * @Version 1.0
 */
@Data
public class InfoCodeConfigGroupInformationVO extends InfoCodeConfigVO {

    /**
     * 分组编码
     */
    private String groupCode;
    /**
     * 分组名称
     */
    private String groupName;

}
