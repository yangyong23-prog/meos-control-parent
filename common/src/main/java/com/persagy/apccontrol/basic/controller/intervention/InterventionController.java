package com.persagy.apccontrol.basic.controller.intervention;

import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.dto.intervention.InterventionInfoQueryListParamDto;
import com.persagy.apccontrol.basic.model.dto.intervention.InterventionQueryTimeSlotDto;
import com.persagy.apccontrol.basic.model.vo.intervention.*;
import com.persagy.apccontrol.basic.service.intervention.InterventionConfigQueryService;
import com.persagy.apccontrol.basic.structure.intervention.model.InterventionConfigData;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 需求干预接口
 *
 * @author yuecaipu
 * @date 2024/7/23 16:01
 * @since v5.240402.00
 */
@Slf4j
@RestController
@RequestMapping(value = "/intervention")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InterventionController<T extends InterventionConfigData> {

    private final InterventionConfigQueryService<T> interventionConfigQueryService;

    @Operation(summary = "查询干预列表", tags = {VersionConstant.V5_240702_0})
    @PostMapping("/config/list")
    public R<List<InterventionQueryListVo<T>>> queryInterventionConfigList(
            @Validated @RequestBody InterventionInfoQueryListParamDto queryParam) {
        return R.success(interventionConfigQueryService.queryInterventionConfig(queryParam));
    }

    @Operation(summary = "查询干预列表时间段", tags = {VersionConstant.V5_240702_0})
    @PostMapping("/config/query/timeslot")
    public R<InterventionConfigTimeSlotVo>queryInterventionTimeSlot(
            @Validated @RequestBody InterventionQueryTimeSlotDto timeSlotDto){
        return R.success(interventionConfigQueryService.queryInterventionTimeSlot(timeSlotDto));
    }

    /**
     * 需求干预详情
     *
     * @param param 入参
     * @return {@link R<ConfigDetailsVo>}
     * @author yuecaipu
     * @date 2024/7/23 16:22
     * @since v5.240702
     */
    @PostMapping(value = "/getConfigDetails")
    public R<ConfigDetailsVo> getConfigDetails(@Validated @RequestBody InterventionQueryParam param) {
        return R.success(interventionConfigQueryService.getConfigDetails(param));
    }

    /**
     * 需求空间统计
     *
     * @param param 入参
     * @return {@link R<SpaceStatisticsVo>}
     * @author yuecaipu
     * @date 2024/7/23 16:23
     * @since v5.240702
     */
    @PostMapping(value = "/spaceStatistics")
    public R<SpaceStatisticsVo> spaceStatistics(@Validated @RequestBody InterventionQueryParam param) {
        return R.success(interventionConfigQueryService.spaceStatistics(param));
    }
}
