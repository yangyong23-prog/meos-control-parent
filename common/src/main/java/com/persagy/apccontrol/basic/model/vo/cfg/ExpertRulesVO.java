package com.persagy.apccontrol.basic.model.vo.cfg;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.entity.RcBasicsConfig;
import com.persagy.apccontrol.basic.model.vo.rules.CoolerStand;
import com.persagy.apccontrol.basic.model.vo.rules.CoolerSwitch;
import com.persagy.apccontrol.basic.model.vo.rules.CoolerTemperature;
import com.persagy.apccontrol.basic.utils.ParameterCheck;
import com.persagy.framework.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Map;

/**
 * 专家规则 配置VO
 *
 * @author yuyao
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExpertRulesVO extends BasicsConfigVO {

    @Schema(description = "项目id" )
    @NotEmpty(message = "{key_d4be1b60bd32403287b0a47366af7092}")
    private String projectId;

    @Schema(description = "规则策略Id，修改时传入")
    private String configId;

    @Schema(description ="系统编号" )
    @NotEmpty(message = "{key_19d7dc7042934ab9a93f77d2ade9956a}")
    private String systemId;

    @Schema(description = "开始日期")
    @NotNull(message = "{key_6dc6cc33082c41d189980dd415b06083}")
    private Date startTime;

    @Schema(description = "结束日期")
    @NotNull(message = "{key_fe10d89294244a90a5a3d488db40d202}")
    private Date endTime;

    @Schema(description = "冷机台数约束")
    @NotNull(message = "{key_7fbea7085096468791a28cf05c1a65b3}")
    @Valid
    private CoolerStand coolerStand;

    @Schema(description = "冷机出水温度")
    @NotNull(message = "{key_7def24dd9e2d41319b55891aa2790e59}")
    @Valid
    private CoolerTemperature coolerTemperature;

    @Schema(description = "冷机开关机约束")
    @NotNull(message = "{key_7a65bdacee6847aaa6ab4b8acf1fa205}")
    @Valid
    private CoolerSwitch coolerSwitch;

    /**
     * 参数校验
     *
     * @param coolerMap 物理世界冷机配置数量
     * @throws BusinessException
     */
    public void check(Map<String, Integer> coolerMap) throws BusinessException {
        ParameterCheck.isDateBefore(startTime, endTime, LocaleMessageUtil.getMessage("key_386891a41a3b469f93c8694a37433695"));
        // 校验冷机台数约束
        coolerStand.check(coolerMap);
        // 校验冷机出水温度
        coolerTemperature.check();
        // 校验冷机开关机约束
        coolerSwitch.check();
    }

    /**
     * 转换VO
     * @param basicsConfig
     * @return
     */
    public ExpertRulesVO convertVo(RcBasicsConfig basicsConfig) {
        ExpertRulesVO expertRulesVO = JSONObject.parseObject(basicsConfig.getResult(), ExpertRulesVO.class);
        BeanUtil.copyProperties(basicsConfig, expertRulesVO);
        expertRulesVO.setConfigId(String.valueOf(basicsConfig.getId()));
        return expertRulesVO;
    }
}
