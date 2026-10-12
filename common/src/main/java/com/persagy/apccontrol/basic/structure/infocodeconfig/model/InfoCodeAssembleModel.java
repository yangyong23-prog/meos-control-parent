package com.persagy.apccontrol.basic.structure.infocodeconfig.model;

import com.persagy.apccontrol.basic.model.base.domain.ValueObject;
import lombok.Data;

/**
 * 信息点组合配置模型
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Data
public class InfoCodeAssembleModel implements ValueObject {

    public static final String ASSEMBLE_SIGN = "|";

    /**
     * 关联信息点
     */
    private InfoCodeConfigModel configModel;

}
