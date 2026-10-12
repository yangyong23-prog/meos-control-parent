package com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent;

import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import com.persagy.apccontrol.basic.outside.vo.riskevent.SimpleRiskEventDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * 异常事件问题
 *
 * @author JiYinchuan
 * @since 5.241101.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ExceptionEventProblemResponse extends BaseResponse {

    /**
     * 实例ID
     */
    private String instanceId;

    /**
     * 实例名称
     */
    private String instanceName;

    /**
     * 问题名称
     */
    private String name;

    /**
     * 未知字段
     */
    private String nodeName;

    /**
     * 对象类型
     */
    private String objectType;

    /**
     * 风险状态
     * <ul>
     *     <li><b>NORMAL</b> - 正常</li>
     *     <li><b>WARN</b> - 超征兆</li>
     *     <li><b>EXCEPTION</b> - 异常</li>
     * </ul>
     */
    private String riskStatus;

    @ApiModelProperty("风险事件")
    private List<SimpleRiskEventDTO> riskEvents;

    /**
     * 关联空间
     */
    private List<ExceptionEventInstanceResponse> relatedSpaces;



}
