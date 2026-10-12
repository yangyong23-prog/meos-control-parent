package com.persagy.apccontrol.basic.controller.instance;

import com.persagy.apccontrol.basic.model.dto.equipmentlist.ConfigEquipmentListHeaderDTO;
import com.persagy.apccontrol.basic.model.param.instance.InstanceConfigQueryParam;
import com.persagy.apccontrol.basic.model.vo.instance.InstanceConfigVO;
import com.persagy.apccontrol.basic.service.instance.config.InstanceConfigService;
import com.persagy.apccontrol.basic.service.instance.config.impl.InstanceConfigEquipmentListHeaderService;
import com.persagy.apccontrol.basic.utils.ParameterCheck;
import com.persagy.framework.common.entity.R;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = "/instanceConfig")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InstanceConfigController {

    private final InstanceConfigService instanceConfigService;
    private final InstanceConfigEquipmentListHeaderService instanceConfigEquipmentListHeaderService;

    /**
     * 查询实例配置列表
     *
     * @return {@code String }
     * @since 7.250601.0
     */
    @PostMapping(value = "/listInstanceConfig")
    public R<List<InstanceConfigVO>> listInstanceConfig(@Valid @RequestBody InstanceConfigQueryParam queryParam) {
        return R.success(instanceConfigService.listInstanceConfig(queryParam));
    }

    /**
     * 查询自定义表头的配置
     * 包含全量设备列表的表头信息
     */
    @PostMapping(value = "/queryConfigEquipmentListHeader")
    public R<ConfigEquipmentListHeaderDTO> queryConfigEquipmentListHeader(
            @RequestBody @Valid InstanceConfigQueryParam queryParam) {
        ParameterCheck.isNull(queryParam.getTemplateTypeId(), "模版 ID不能为空");
        return R.success(instanceConfigEquipmentListHeaderService.listAllConfigAndHeader(queryParam));
    }
}
