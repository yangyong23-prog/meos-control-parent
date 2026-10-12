package com.persagy.apccontrol.basic.outside.feign.model.param.midpermission;

import com.persagy.apccontrol.basic.outside.feign.model.param.OriginUrlBaseParam;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.Collection;

/**
 * 有效项目查询参数
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
public class ProjectValidQueryParam extends OriginUrlBaseParam {

    /**
     * 当前用户
     */
    private ProjectUserQueryParam puser = new ProjectUserQueryParam();

    /**
     * 集团编码
     */
    @NotBlank(message = "{key_c0659325aa57425499811b4a531f4315}")
    private String groupCode;

    /**
     * 项目ID, 为空时查询所有有效项目
     */
    private Collection<String> projectIds;

}
