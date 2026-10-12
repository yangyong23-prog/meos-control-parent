package com.persagy.apccontrol.basic.controller.outside;

import com.persagy.apccontrol.basic.constant.ExternalServerConstant;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.vo.outside.SystemGraphVO;
import com.persagy.apccontrol.basic.outside.feign.service.SystemGraphExternalService;
import com.persagy.framework.common.entity.R;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

@RestController
@Api(tags = "系统图外部接口")
@RequestMapping("/external/systemGraph")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SystemGraphExternalController {

    private final SystemGraphExternalService systemGraphExternalService;

    /**
     * 查询系统图列表
     *
     * @param classCodes 设备类编码
     * @return {@link R }<{@link Object }>
     */
    @Operation(summary = "查询-系统图列表", tags = {
            VersionConstant.V5_241001_0,
            ExternalServerConstant.MEOI_AIM_SERVER
    })
    @PostMapping("/querySystemGraphList")
    public R<List<SystemGraphVO>> querySystemGraphList(@RequestBody Collection<String> classCodes) {
        return R.success(systemGraphExternalService.querySystemGraphList(classCodes));
    }
}
