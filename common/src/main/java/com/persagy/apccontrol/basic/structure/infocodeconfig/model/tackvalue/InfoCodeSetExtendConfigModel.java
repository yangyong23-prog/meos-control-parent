package com.persagy.apccontrol.basic.structure.infocodeconfig.model.tackvalue;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

/**
 * 冷却塔出水温度设置配置
 */
@Data
@ToString
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InfoCodeSetExtendConfigModel extends BaseInfoCodeTackValueLogicModel {
    
    /**
     * 信息点配置
     */
    @NotBlank
    private String infoCode;
    
    /**
     * 获取日志标签
     *
     * @return 日志标签
     * @since 5.240501.0
     */
    @Override
    public String logTag() {
        return "信息点是否存在配置解析逻辑";
    }
}
