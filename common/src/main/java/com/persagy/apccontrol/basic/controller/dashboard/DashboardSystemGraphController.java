package com.persagy.apccontrol.basic.controller.dashboard;

import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.param.dashboard.QuerySystemCardParam;
import com.persagy.apccontrol.basic.model.vo.dashboard.SystemCardVO;
import com.persagy.apccontrol.basic.service.dashboard.DashboardSystemGraphService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * dashboard系统图控制器
 *
 * @author MuAnFu
 * @date 2025-05-26
 */
@RestController
@RequestMapping("/dashboard/systemGraph")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DashboardSystemGraphController {

    private final DashboardSystemGraphService dashboardSystemGraphService;

    @Operation(summary = "通过一级菜单Code获取系统列表", tags = VersionConstant.V7_250501_0)
    @PostMapping("/listSystemByFirstMenuCode/{menuCode}")
    public R<List<SystemCardVO>> listSystemByFirstMenuCode(@PathVariable String menuCode, @RequestBody QuerySystemCardParam param) {
        return R.success(dashboardSystemGraphService.listSystemByFirstMenuCode(menuCode, param));
    }
}
