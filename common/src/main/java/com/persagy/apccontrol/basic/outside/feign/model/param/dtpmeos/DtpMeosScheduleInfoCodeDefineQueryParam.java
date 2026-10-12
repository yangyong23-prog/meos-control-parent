package com.persagy.apccontrol.basic.outside.feign.model.param.dtpmeos;

import com.persagy.framework.common.context.ContextUtil;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 查询-冰蓄冷需求计算结果参数
 *
 * @author JiYinchuan
 * @since 7.260201.0
 */
@Data
public class DtpMeosScheduleInfoCodeDefineQueryParam {

    /**
     * 项目ID
     */
    @NotBlank(message = "Project id can not be blank")
    private String projectId = ContextUtil.getProjectId();

    /**
     * 设备类编码
     */
    @NotBlank(message = "ClassCode id can not be blank")
    private String classCode;

}
