package com.persagy.apccontrol.basic.controller;

import com.persagy.framework.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 通用多虑器异常转发
 *
 * @author shaohongbo
 * @date 2023/3/24 23:55
 */
@Tag(name = "异常转发controller")
@RestController
public class ExceptionController {


    @RequestMapping("/error/exThrow")
    public void rethrow(HttpServletRequest request) throws Exception {
        Object attribute = request.getAttribute("filter.error");
        if (BusinessException.class.isInstance(attribute)) {
            throw (BusinessException) attribute;
        }
        throw (Exception) attribute;
    }

}
