package com.persagy.apccontrol.controller.instance;

import com.persagy.apccontrol.basic.model.param.instance.InstanceConfigSaveParam;
import com.persagy.apccontrol.basic.service.instance.config.InstanceConfigService;
import com.persagy.framework.common.entity.R;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/instanceConfig")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InstanceConfigCloudController {

    private final InstanceConfigService instanceConfigService;

    /**
     * 保存实例配置
     *
     * @return {@code R<Boolean> }
     * @since 7.250601.0
     */
    @PostMapping(value = "/saveInstanceConfig")
    public R<Boolean> saveInstanceConfig(@Valid @RequestBody InstanceConfigSaveParam param) {
        return R.success(instanceConfigService.saveInstanceConfig(param, false));
    }
}
