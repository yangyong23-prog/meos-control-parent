package com.persagy.apccontrol.basic.controller.security;

import com.persagy.apccontrol.basic.model.param.security.SecurityDashboardQueryParam;
import com.persagy.apccontrol.basic.model.param.security.SecurityModelViewParam;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdSpaceRelObjVO;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdSpaceVO;
import com.persagy.apccontrol.basic.model.vo.security.*;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.service.common.CommonConfigService;
import com.persagy.apccontrol.basic.service.security.SecurityService;
import com.persagy.framework.common.entity.R;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

import static com.persagy.apccontrol.basic.constant.VersionConstant.V5_240801_0;
import static com.persagy.apccontrol.basic.constant.VersionConstant.V5_241201_0;

/**
 * 空间安防
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/8/5 14:29
 * @since v5.240801.0
 */
@RestController
@RequestMapping(value = "/security")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Api(value = "空间安防", tags = V5_240801_0)
public class SecurityController {

    private final SecurityService securityService;
    private final CommonConfigService commonConfigService;

    @Operation(summary = "查询-查询空间安防模型视图", tags = V5_241201_0)
    @PostMapping("/dashboard")
    public R<SecurityDashboardVO> dashboard(@Valid @RequestBody SecurityDashboardQueryParam param) {
        return R.success(securityService.dashboard(param));
    }


    /**
     * 查询空间安防模型视图
     *
     * @param param param
     * @return {@link R }<{@link SecurityModelViewVO }>
     */
    @Operation(summary = "查询-查询空间安防模型视图", tags = V5_240801_0)
    @PostMapping("/querySecurityModelView")
    public R<SecurityModelViewVO> querySecurityModelView(@Validated @RequestBody SecurityModelViewParam param) {
        return R.success(securityService.querySecurityModelView(param));
    }

    @PostMapping("/listDeploymentSpace")
    public R<List<DtpRwdSpaceVO>> listDeploymentSpace(@Validated @RequestBody SecurityModelViewParam param) {
        return R.success(securityService.listDeploymentSpace());
    }

    @PostMapping("/listAccessSpace")
    public R<List<DtpRwdSpaceRelObjVO>> listAccessSpace(@Validated @RequestBody SecurityModelViewParam param) {
        return R.success(securityService.listAccessSpace());
    }

    @PostMapping("/listEquipment")
    public R<List<SecurityEquipmentStatusVO>> listEquipment(@Validated @RequestBody SecurityModelViewParam param) {
        return R.success(securityService.listEquipment());
    }

    /**
     * 布放空间统计
     *
     * @param param param
     * @return
     */
    @Operation(summary = "布放空间统计", tags = V5_240801_0)
    @PostMapping("/arming/space/statistics")
    public R<SecuritySpaceStatisticsVo> getArmingSpaceStatistics(@Validated @RequestBody SecurityModelViewParam param) {
        return R.success(securityService.getArmingSpaceStatistics(param));
    }


    /**
     * 通行空间统计
     *
     * @param param param
     * @return
     */
    @Operation(summary = "通行空间统计", tags = V5_240801_0)
    @PostMapping("/access/space/statistics")
    public R<SecurityAccessSpaceStatisticsVo> getAccessSpaceStatistics(@Validated @RequestBody SecurityModelViewParam param) {
        return R.success(securityService.getAccessSpaceStatistics(param));
    }

    @Operation(summary = "安防设备统计", tags = V5_240801_0)
    @PostMapping("/equipment/statistics")
    public R<List<SecurityEquipmentTypeStatisticsVo>> equipmentStatistics(@Validated @RequestBody SecurityModelViewParam param) {
        return R.success(securityService.getEquipmentTypeStatistics(param));
    }


    /**
     * 查询空间安防视角的异常事件
     *
     * @param param param
     * @return {@link R }<{@link List }<{@link ExceptionEventBaseInfoVO }>>
     */
    @Operation(summary = "查询-查询空间安防视角的异常事件", tags = V5_240801_0)
    @PostMapping("/getAbnormalDataList")
    public R<List<ExceptionEventBaseInfoVO>> getAbnormalDataList(@Validated @RequestBody SecurityModelViewParam param) {
        return R.success(securityService.getAbnormalDataList(param));
    }

    /**
     * 查询-查询筛选分组
     *
     * @return {@link R }<{@link List }<{@link SecurityScreenConfigVO }>>
     */
    @Operation(summary = "查询-查询筛选分组", tags = V5_240801_0)
    @PostMapping("/screenConfig")
    public R<List<SecurityScreenConfigVO>> getScreenConfigList() {
        return R.success(commonConfigService.getScreenConfigList());
    }

    /**
     * 查询-安防设备实例数量统计(安防功能最低要求)
     *
     * @return {@link R }<SecurityFunctionVO>
     */
    @Operation(summary = "查询-安防设备实例数量统计(安防功能最低要求)", tags = V5_241201_0)
    @PostMapping("/function")
    public R<SecurityFunctionVO> querySecurityFunction() {
        return R.success(securityService.querySecurityFunction());
    }

}
