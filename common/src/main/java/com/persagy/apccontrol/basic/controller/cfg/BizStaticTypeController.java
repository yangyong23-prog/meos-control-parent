package com.persagy.apccontrol.basic.controller.cfg;

import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.model.base.BizStaticTypeVO;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * 静态业务类型 接口
 *
 * @author yuyao
 */
@RestController
@RequestMapping("/bizStaticType")
@Slf4j
@Validated
@Tag(name = "静态业务类型 接口")
public class BizStaticTypeController {

    /**
     * @Description 配置类型枚举
     * @Author yuyao
     */
    @Operation(summary = "配置类型枚举")
    @GetMapping("getBasicsConfigType")
    public R<List<BizStaticTypeVO>> getPrePay() {
        return R.success(BasicsConfigTypeEnum.getList());
    }


}
