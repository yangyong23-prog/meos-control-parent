package com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent;

import com.persagy.apccontrol.basic.outside.enums.abnormalevent.ImportanceDegreeEnum;
import com.persagy.apccontrol.basic.outside.enums.abnormalevent.RiskMeasureAimLevel1;
import com.persagy.apccontrol.basic.outside.enums.abnormalevent.RiskMeasureAimLevel2;
import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import com.persagy.apccontrol.basic.outside.vo.riskevent.SimpleRiskEventDTO;
import com.persagy.apccontrol.basic.outside.vo.riskevent.SolveProcessEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * 异常事件响应
 *
 * @author JiYinchuan
 * @since 5.241101.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ExceptionEventResponse extends BaseResponse {

    /**
     * 异常事件ID
     */
    private String id;

    /**
     * 异常事件状态
     * <ul>
     *     <li><b>UNRESOLVED</b> - 未解决</li>
     *     <li><b>RESOLVED</b> - 已解决</li>
     * </ul>
     */
    private String exceptionEventStatus;

    /**
     * 过时, 异常事件编码, 就是异常事件ID了
     */
    private String exceptionEventCode;

    /**
     * 异常事件开始时间
     */
    private Long exceptionStartTime;

    /**
     * 异常事件结束时间
     */
    private Long exceptionEndTime;

    /**
     * 持续时间, 单位分钟
     */
    private Integer duration;

    /**
     * 所有问题数量
     */
    private Integer allProblemCount;

    /**
     * 严重程度
     *
     * @see ImportanceDegreeEnum
     */
    private String importanceDegree;

    /**
     * 实例列表
     */
    private List<ExceptionEventInstanceResponse> instances;

    /**
     * 实际影响
     */
    private List<ExceptionEventImpactCategoryListResponse> impactCategoryList;

    /**
     * 问题列表
     */
    private List<ExceptionEventProblemResponse> problems;

    /**
     * 已解决问题数量
     */
    private Integer resolvedProblemCount;

    /**
     * 一级风控目标
     *
     * @see RiskMeasureAimLevel1
     */
    private List<String> riskMeasureAimLevel1Collect;

    /**
     * 二级风控目标
     *
     * @see RiskMeasureAimLevel2
     */
    private List<String> riskMeasureAimLevel2Collect;

    //--------------------------250701新增字段start----------------------
    @ApiModelProperty("始发问题")
    private ExceptionEventProblemResponse originalProblem;

    @ApiModelProperty("最早要求解决时间")
    private Long minRequireSolveTime;

    @ApiModelProperty("最晚要求解决时间")
    private Long maxRequireSolveTime;
    @ApiModelProperty("异常事件是否已逾期")
    private boolean overdue;

    @ApiModelProperty("关联风险事件")
    private List<SimpleRiskEventDTO> relatedRiskEvents;

    @ApiModelProperty("处理进程")
    private SolveProcessEnum solveProcess;

    /**
     * 事件编号
     */
    private String eventNumber;
    //--------------------------250701新增字段end----------------------

}
