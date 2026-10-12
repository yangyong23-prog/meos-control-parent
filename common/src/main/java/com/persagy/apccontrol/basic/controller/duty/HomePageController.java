package com.persagy.apccontrol.basic.controller.duty;

import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.param.duty.HomePageParam;
import com.persagy.apccontrol.basic.model.vo.duty.BasicsCardVO;
import com.persagy.apccontrol.basic.service.duty.HomePageService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 值班首页接口
 */
@Slf4j
@RestController
@RequestMapping("/duty")
public class HomePageController<T extends BasicsCardVO> {


    @Resource
    HomePageService<T> homePageService;

    @Operation(summary = "查询-值班首页各系统统计状态", tags = {VersionConstant.V5_241101_0})
    @PostMapping("/homePage")
    public R<List<T>> homePage(@Validated @RequestBody HomePageParam param) {
        return R.success(homePageService.homePage(param));
    }

}
