package com.persagy.apccontrol.basic.controller;

import com.persagy.apccontrol.basic.convert.HumitureTargetConvert;
import com.persagy.apccontrol.basic.model.enumtype.HumidityUpperTypeEnum;
import com.persagy.apccontrol.basic.model.vo.BaseEnumVO;
import com.persagy.apccontrol.basic.model.vo.HumidityUpperTypeVO;
import com.persagy.apccontrol.basic.structure.map.OpenEnumKeyValue;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 静态对象管理
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
@Tag(name = "静态对象管理")
@Validated
@RestController
@RequestMapping("/static")
public class StaticController {

    @Autowired
    private HumitureTargetConvert humitureTargetConvert;
    @Autowired
    private OpenEnumKeyValue openEnumKeyValue;

    @Operation(summary = "[查询] 公开枚举")
    @PostMapping("/listOpenEnum/{key}")
    public R<List<BaseEnumVO>> listOpenEnum(@PathVariable String key) {
        return R.success(openEnumKeyValue.getEnumConstants(key));
    }

    @Operation(summary = "[查询-温湿度目标] 湿度上限类型")
    @PostMapping("/humiture/listHumidityUpperType")
    public R<List<HumidityUpperTypeVO>> listHumidityUpperType() {
        return R.success(humitureTargetConvert.toVo(HumidityUpperTypeEnum.values()));
    }
}
