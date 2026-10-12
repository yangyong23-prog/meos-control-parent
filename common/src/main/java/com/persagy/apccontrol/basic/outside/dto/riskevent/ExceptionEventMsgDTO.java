package com.persagy.apccontrol.basic.outside.dto.riskevent;

import com.persagy.apccontrol.basic.outside.enums.ExceptionEventStatus;
import com.persagy.apccontrol.basic.outside.enums.abnormalevent.ImportanceDegreeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.ExceptionEventProblemResponse;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.outside.vo.riskevent.SimpleRiskEventDTO;
import com.persagy.apccontrol.basic.outside.vo.riskevent.SolveProcessEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 异常事件队列消息
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/10/18 11:31
 * @since v5.241002.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ApiModel("异常事件队列消息")
public class ExceptionEventMsgDTO {

    @ApiModelProperty("异常事件id")
    private Long id;

    @ApiModelProperty("项目id")
    private String projectId;

    @ApiModelProperty("集团编码")
    private String groupCode;

    @ApiModelProperty("一级风控目标")
    private Set<String> riskMeasureAimLevel1Collect;

    @ApiModelProperty("二级风控目标")
    private Set<String> riskMeasureAimLevel2Collect;

    @ApiModelProperty("所属异常事件状态")
    private ExceptionEventStatus exceptionEventStatus;

    @ApiModelProperty("对象实例列表")
    private List<ExceptionEventBaseInfoVO.ExceptionInstance> instances;

    @ApiModelProperty("问题列表")
    private List<ExceptionEventBaseInfoVO.ExceptionProblem> problems;

    @ApiModelProperty("始发时间")
    private String exceptionStartTime;

    @ApiModelProperty("结束时间")
    private String exceptionEndTime;

    @ApiModelProperty("状态更新时间")
    private String exceptionStatusUpdateTime;

    @ApiModelProperty("已解决问题数量")
    private Integer resolvedProblemCount;

    @ApiModelProperty("所有问题数量")
    private Integer allProblemCount;

    @ApiModelProperty("严重程度")
    private ImportanceDegreeEnum importanceDegree;

    @ApiModelProperty("持续时间,单位分钟")
    private Long duration;

    //--------------------------250701新增字段start----------------------
    @ApiModelProperty("始发问题")
    private ExceptionEventProblemResponse originalProblem;

    @ApiModelProperty("最早要求解决时间")
    private LocalDateTime minRequireSolveTime;

    @ApiModelProperty("最晚要求解决时间")
    private LocalDateTime maxRequireSolveTime;

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
