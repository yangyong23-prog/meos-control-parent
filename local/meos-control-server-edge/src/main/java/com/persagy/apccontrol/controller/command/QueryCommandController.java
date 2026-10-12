package com.persagy.apccontrol.controller.command;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 查询需求指令
 *
 * @author yuyao
 */

@Tag(name = "查询系统需求参数")
@Validated
@RestController
@RequestMapping("/queryCommand")
@Slf4j
public class QueryCommandController {

    //移除已迁移的接口



}
