package com.persagy.apccontrol.basic.controller.ventilation;

import com.persagy.apccontrol.basic.model.param.ventilation.VentilationModelViewParam;
import com.persagy.apccontrol.basic.model.vo.kitchen.KitChenRangeHoodEquipStatisticsVo;
import com.persagy.apccontrol.basic.model.vo.ventilation.VentilationModelViewVO;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.service.ventilation.VentilationIotService;
import com.persagy.apccontrol.basic.service.ventilation.VentilationService;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.entity.R;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.persagy.apccontrol.basic.constant.VersionConstant.V5_240702_0;
import static com.persagy.apccontrol.basic.constant.VersionConstant.V7_250201_0;

@RestController
@RequestMapping(value = "/ventilation")
@Api(value = "通风", tags = V5_240702_0)
public class VentilationController {

    @Resource
    private VentilationService ventilationService;

    @Resource
    private VentilationIotService ventilationIotService;
    /**
     * 查询通风模型视图
     *
     * @param param param
     * @return {@link R }<{@link Collection }<{@link VentilationModelViewVO }>>
     */
    @Operation(summary = "查询-查询通风模型视图", tags = V5_240702_0)
    @PostMapping("/queryVentilationModelView")
    public R<VentilationModelViewVO> queryVentilationModelView(@Validated @RequestBody VentilationModelViewParam param) {
        return R.success(ventilationService.queryVentilationModelView(param));
    }

    /**
     * 查询通风模型视图
     *
     * @param param param
     * @return {@link R }<{@link Collection }<{@link VentilationModelViewVO }>>
     */
    @Operation(summary = "查询-查询通风模型视图", tags = V5_240702_0)
    @PostMapping("/getAbnormalDataList")
    public R<List<ExceptionEventBaseInfoVO>> getAbnormalDataList(@Validated @RequestBody VentilationModelViewParam param) {
        return R.success(ventilationService.getAbnormalDataList(param));
    }

    @Operation(summary = "查询-查询通风模型视图", tags = V5_240702_0)
    @PostMapping("/mapClaasCode2objectIds")
    public R<Map<String, Set<String>>> mapClaasCode2objectIds(@Validated @RequestBody VentilationModelViewParam param) {
        return R.success(ventilationService.mapClaasCode2objectIds(ContextUtil.getProjectId(), param.getInstanceId(), param.getInstanceType()));
    }


    @Operation(summary = "查询-设备统计接口", tags = V7_250201_0)
    @PostMapping("/equipment/statistics")
    public R<Map<String, KitChenRangeHoodEquipStatisticsVo>> queryEquipStatistics(@RequestBody Map<String,
            Set<String>> param) {
        return R.success(ventilationIotService.queryEquipStatistics(param));
    }

}
