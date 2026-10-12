package com.persagy.apccontrol.controller.command;


import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yuyao
 */
@Tag(name = "系统参数设定")
@Validated
@RestController
@RequestMapping("/commandExecute")
@Slf4j
public class CommandExecuteController {

    //移除已迁移的接口
}
