package com.persagy.apccontrol.basic.controller.airteminal;

import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.dto.SingleObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.param.airterminal.*;
import com.persagy.apccontrol.basic.model.param.demand.DemandSpaceByGroupFloorQueryParam;
import com.persagy.apccontrol.basic.model.param.demand.airconditioning.SpaceInfoQueryParam;
import com.persagy.apccontrol.basic.model.param.demand.airconditioning.TagInfoQueryParam;
import com.persagy.apccontrol.basic.model.vo.TagInfoVO;
import com.persagy.apccontrol.basic.model.vo.airterminal.*;
import com.persagy.apccontrol.basic.model.vo.airterminal.statistics.DashboardEquipmentVO;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.service.airterminal.AirTerminalService;
import com.persagy.apccontrol.basic.structure.airterminal.converter.AirTerminalConverter;
import com.persagy.apccontrol.basic.utils.ParameterCheck;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import static com.persagy.apccontrol.basic.constant.VersionConstant.*;

/**
 * 空调末端控制器
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@RestController
@RequestMapping(value = "/airTerminal")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AirTerminalController {

    private final AirTerminalService airTerminalService;

    /**
     * 空调末端接口响应转换器。
     *
     * @since v7.260701.0
     */
    private final @NonNull AirTerminalConverter airTerminalConverter;

    @Operation(summary = "查询-Dashboard设备统计显隐", tags = V7_251101_0)
    @PostMapping(value = "/statistic/dashboardEquipmentVisible")
    public R<Boolean> statisticDashboardEquipmentVisible() {
        return R.success(airTerminalService.statisticDashboardEquipmentVisible());
    }

    @Operation(summary = "查询-Dashboard设备统计", tags = V7_251101_0)
    @PostMapping(value = "/statistic/dashboardEquipment")
    public R<Collection<DashboardEquipmentVO>> statisticDashboardEquipment() {
        return R.successMulti(airTerminalService.statisticDashboardEquipment());
    }

    @Operation(summary = "查询-品质区间", tags = V5_240502_0)
    @PostMapping(value = "/getQualityInterval")
    public R<AirTerminalQualityIntervalVO> getQualityInterval(
            @RequestBody @Valid AirTerminalQualityIntervalQueryParam param) {
        return R.success(airTerminalService.getQualityInterval(param));
    }

    @Operation(summary = "查询-需求状态", tags = V5_240502_0)
    @PostMapping(value = "/getDemandStatus")
    public R<AirTerminalDemandStatusVO> getDemandStatus(
            @RequestBody @Valid AirTerminalDemandStatusQueryParam param) {
        return R.success(airTerminalService.getDemandStatus(param));
    }

    @Operation(summary = "查询-室外天气", tags = V5_240502_0)
    @PostMapping(value = "/getOutdoorWeather")
    public R<AirTerminalOutdoorWeatherVO> getOutdoorWeather(
            @RequestBody @Valid AirTerminalOutdoorWeatherQueryParam param) {
        return R.success(airTerminalService.getOutdoorWeather(param));
    }

    @Operation(summary = "查询-空间品质统计", tags = V5_240502_0)
    @PostMapping(value = "/getSpaceQualityStatistics")
    public R<AirTerminalSpaceQualityStatisticsVO> getSpaceQualityStatistics(
            @RequestBody @Valid AirTerminalSpaceQualityStatisticsQueryParam param) {
        return R.success(airTerminalService.getSpaceQualityStatistics(param));
    }

    @Operation(summary = "查询-空间带状态", tags = V5_240502_0)
    @PostMapping(value = "/listSpaceWithStatus")
    public R<AirTerminalSpaceWithStatusVO> listSpaceWithStatus(
            @RequestBody @Valid AirTerminalSpaceWithStatusQueryParam param) {
        return R.success(airTerminalService.listSpaceWithStatus(param));
    }

    /**
     * 查询空间和设备的对象基础信息及末端业务状态。
     *
     * <p>模型视图字段由前端通过独立模型图接口获取，本接口也不返回空间温度和二氧化碳
     * 规则标记。</p>
     *
     * @param param 空间设备查询参数
     * @return 不包含模型视图字段及温度、二氧化碳规则标记的空间设备结果
     */
    @Operation(summary = "查询-空间设备信息", tags = V7_250101_0)
    @PostMapping(value = "/listSpaceEquipment")
    public @NonNull R<AirTerminalSpaceEquipmentVO> listSpaceEquipment(
            @RequestBody @Valid @NonNull AirTerminalSpaceWithStatusQueryParam param) {
        AirTerminalSpaceWithStatusVO result =
                this.airTerminalService.listSpaceEquipment(param);
        return R.success(this.airTerminalConverter.toSpaceEquipmentVo(result));
    }

    @Operation(summary = "查询-空间温度统计（首页看板）", tags = V5_240502_0)
    @PostMapping(value = "/listSpaceTemperatureStatistics")
    public R<AirTerminalSpaceTemperatureStatisticsVO> listSpaceTemperatureStatistics(
            @RequestBody @Valid AirTerminalSpaceWithStatusQueryParam param) {
        return R.success(airTerminalService.listSpaceTemperatureStatistics(param));
    }

    @Operation(summary = "查询-设备类列表", tags = V5_240502_0)
    @PostMapping(value = "/listEquipmentTypes")
    public R<Collection<AirTerminalEquipmentTypeVO>> listEquipmentTypes(
            @RequestBody @Valid AirTerminalEquipmentTypeQueryParam param) {
        return R.successMulti(airTerminalService.listEquipmentTypes(param));
    }

    @Operation(summary = "查询-楼层下管道列表", tags = V5_240502_0)
    @PostMapping(value = "/listPipelineByFloor")
    public R<Collection<DtpRwdObjectDTO>> listPipelineByFloor(
            @RequestBody @Valid AirTerminalPipelineByFloorQueryParam param) {
        return R.successMulti(airTerminalService.listPipelineByFloor(param));
    }

    @Deprecated
    @Operation(summary = "查询-设备状态统计", tags = V5_240502_0)
    @PostMapping(value = "/listEquipmentStatusStatistics")
    public R<Collection<AirTerminalFloorEquipmentTypeStatisticsVO>> listEquipmentStatusStatistics(
            @RequestBody @Valid AirTerminalEquipmentStatusStatisticsQueryParam param) {
        return R.successMulti(airTerminalService.listEquipmentStatusStatistics(param));
    }

    @Operation(summary = "设备名称标签", tags = VersionConstant.V5_240502_0)
    @PostMapping("/listEquipmentTag")
    public R<TagInfoVO> listEquipmentTag(
            @Validated @RequestBody SpaceInfoQueryParam param) {
        return R.success(airTerminalService.listEquipmentTag(param));
    }

    @Operation(summary = "查询-设备控制台iot信息", tags = VersionConstant.V5_240502_0)
    @PostMapping("/listEquipmentControllerDetails")
    public R<Object> listEquipmentControllerDetails(
            @Validated @RequestBody SpaceInfoQueryParam param) {
        return R.success(airTerminalService.listEquipmentControllerDetails(param));
    }

    @Operation(summary = "查询-异常信息列表（空调末端）", tags = V5_240502_0)
    @PostMapping(value = "/getAbnormalDataList")
    public R<List<ExceptionEventBaseInfoVO>> getAbnormalDataList(@RequestBody @Valid AirTerminalAbnormalQueryParam instanceQueryParam) {
        return R.success(airTerminalService.getAbnormalDataList(instanceQueryParam));
    }

    @Operation(summary = "查询-对象标签信息", tags = V5_240502_0)
    @PostMapping(value = "/getObjectTag")
    public R<TagInfoVO> getObjectTag(@RequestBody @Valid TagInfoQueryParam tagInfoQueryParam) {
        return R.success(airTerminalService.getObjectTag(tagInfoQueryParam));
    }

    @Operation(summary = "查询手自动模式查询", tags = {VersionConstant.V5_8_0})
    @PostMapping("/queryManualAutoStatusByObjectId")
    public R<AirTerminalEquipManualAutoStatusVo> queryManualAutoStatusByObjectId(@Validated @RequestBody SingleObjectDTO dto) {
        ParameterCheck.isTrue(StringUtils.isBlank(dto.getProjectId()), LocaleMessageUtil.getMessage("key_ccd21ed6ba89436194c304f8bdd8db01"));
        ParameterCheck.isTrue(StringUtils.isBlank(dto.getObjectId()), LocaleMessageUtil.getMessage("key_af2731be4b1b4586a671d4016567cdcd"));
        ParameterCheck.isTrue(StringUtils.isBlank(dto.getSystemClassCode()), LocaleMessageUtil.getMessage("key_e42a9ce9d6ac435e9ec8917dc922cc53"));
        AirTerminalEquipManualAutoStatusVo result = airTerminalService.queryManualAutoStatusByObjectId(dto);
        return R.success(result);
    }

    @Operation(summary = "调度数据历史回放", tags = {VersionConstant.V5_8_0})
    @PostMapping("/historyPlaybackByTime")
    public R<TreeMap<LocalTime, AirTerminalHistoryPlaybackVO>> historyPlaybackByTime(@Validated @RequestBody AirTerminalHistoryPlaybackParam param) throws Exception {
        if (StringUtils.isBlank(param.getProjectId())) {
            param.setProjectId(ContextUtil.getProjectId());
        }
        TreeMap<LocalTime, AirTerminalHistoryPlaybackVO> historyPlaybackVO = this.airTerminalService.historyPlaybackByTime(ContextUtil.getGroupCode(), param);
        return R.success(historyPlaybackVO);
    }


    @Operation(summary = "查询-弹框继续加载", tags = VersionConstant.V7_250301_0)
    @PostMapping(value = "/getDialogContinueLoad")
    public R<Boolean> getDialogContinueLoad(@RequestBody @Valid DemandSpaceByGroupFloorQueryParam param) {
        return R.success(airTerminalService.getDialogContinueLoad(param));
    }
}
