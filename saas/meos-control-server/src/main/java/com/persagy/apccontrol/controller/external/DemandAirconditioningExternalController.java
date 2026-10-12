package com.persagy.apccontrol.controller.external;

import com.persagy.apccontrol.basic.constant.ExternalServerConstant;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.param.demand.airconditioning.DemandAirconditioningObjectQueryParam;
import com.persagy.apccontrol.basic.model.vo.demand.airconditioning.DemandAirconditioningObjectVO;
import com.persagy.apccontrol.service.external.DemandAirconditioningExternalService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 需求管理外部接口控制器
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@RestController
@RequestMapping("/external/demand/airconditioning")
public class DemandAirconditioningExternalController {

    @Autowired
    private DemandAirconditioningExternalService demandAirconditioningExternalService;

    @Operation(summary = "查询-管控对象列表", tags = {
            VersionConstant.V5_7_0,
            ExternalServerConstant.AI_AU_SERVER
    })
    @PostMapping("/listControlledObject")
    @Deprecated
    public R<List<DemandAirconditioningObjectVO>> listControlledObject(
            @RequestBody @Valid DemandAirconditioningObjectQueryParam param) {
        return R.success(demandAirconditioningExternalService.listControlledObject(param));
    }
}
