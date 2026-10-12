package com.persagy.apccontrol.controller;

import com.persagy.apccontrol.basic.model.enumtype.system.RunModeEnum;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @Description: 运行模式
 * @author: yancheng
 * @date: 2023/3/21
 * @version: V1.0
 */
@Tag(name ="运行模式")
@Validated
@RestController
@RequestMapping("/runMode")
@Slf4j
public class RunModeController {

    /**
     * 运行模式-枚举获取
     * @param code
     * @return
     */
    @Operation(summary = "运行模式-枚举获取")
    @GetMapping("/getRunModeEnum/{code}")
    public R<Map<String,String>> getRunModeEnum(@PathVariable String code){
        return R.success(RunModeEnum.getEnumMapByCode(code));
    }
}
