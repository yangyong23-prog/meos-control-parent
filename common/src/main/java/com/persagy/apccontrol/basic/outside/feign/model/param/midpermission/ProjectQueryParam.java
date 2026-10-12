package com.persagy.apccontrol.basic.outside.feign.model.param.midpermission;

import com.persagy.apccontrol.basic.outside.feign.model.param.OriginUrlBaseParam;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 项目查询参数
 *
 * @author JiYinchuan
 * @since v5.0.0
 */
@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
public class ProjectQueryParam extends OriginUrlBaseParam {

    /**
     * 当前用户
     */
    private ProjectUserQueryParam puser = new ProjectUserQueryParam();

    /**
     * 集团编码
     */
    private String groupCode;

    /**
     * 项目ID
     */
    private String projectId;

}
