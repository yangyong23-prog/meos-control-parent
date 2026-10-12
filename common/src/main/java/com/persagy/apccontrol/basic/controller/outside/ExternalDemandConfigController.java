package com.persagy.apccontrol.basic.controller.outside;

import com.persagy.apccontrol.basic.constant.ExternalServerConstant;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.param.external.demand.ExternalDemandConfigResultQueryParam;
import com.persagy.apccontrol.basic.model.vo.external.demand.ExternalDemandConfigResultVO;
import com.persagy.apccontrol.basic.service.demand.external.ExternalDemandConfigService;
import com.persagy.framework.common.entity.R;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@Api(tags = "需求管理配置外部接口")
@RequestMapping("/external/demand/config")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ExternalDemandConfigController {
    
    private final ExternalDemandConfigService externalDemandConfigService;
    
    /**
     * 查询需求配置结果
     *
     * @param queryParam 查询参数
     * @return {@link R }<{@link Object }>
     */
    @Operation(summary = "查询-查询需求配置结果", tags = {
            VersionConstant.V7_250602_0,
            ExternalServerConstant.MEOI_AIM_SERVER
    })
    @PostMapping("/listConfigResultsByInstance")
    public R<List<? extends ExternalDemandConfigResultVO>> listConfigResultsByInstance(@RequestBody @Valid ExternalDemandConfigResultQueryParam queryParam) {
        return R.success(externalDemandConfigService.listConfigResultsByInstance(queryParam));
    }
    
}
