package com.persagy.apccontrol.basic.model.vo.bizcheck;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonObject;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.structure.bizcheck.enumeration.BizCheckStatusEnum;
import com.persagy.apccontrol.basic.structure.bizcheck.enumeration.BizCheckStrategyTypeEnum;
import com.persagy.apccontrol.basic.structure.bizcheck.enumeration.BizCheckTypeEnum;
import com.persagy.apccontrol.basic.structure.bizcheck.model.dto.BizCheckItemDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 业务检查结果响应VO
 *
 * @author wxf
 * @date 2026-03-02
 */
@Data
public class BizCheckResultVO {

    /**
     * 检查结果ID
     */
    @Schema(description = "检查结果ID")
    private Long id;

    /**
     * 业务条线编码
     */
    @Schema(description = "业务条线编码")
    private RunningMonitorTypeEnum bizCode;

    /**
     *检查项编码
     */
    @Schema(description = "检查项编码")
    private BizCheckStrategyTypeEnum checkItemCode;

    /**
     * 检查状态
     */
    @Schema(description = "检查状态")
    private BizCheckStatusEnum checkStatus;

    /**
     * 检查类型
     */
    @Schema(description = "检查类型")
    private BizCheckTypeEnum checkType;

    /**
     * 检查项结果详情
     */
    @Schema(description = "检查项结果详情")
    private JsonNode checkResult;

    /**
     * 检查结束时间
     */
    @Schema(description = "检查结束时间")
    private LocalDateTime checkEndTime;
}