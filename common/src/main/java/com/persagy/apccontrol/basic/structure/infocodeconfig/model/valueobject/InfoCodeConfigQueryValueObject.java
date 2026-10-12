package com.persagy.apccontrol.basic.structure.infocodeconfig.model.valueobject;

import lombok.Data;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * 信息点配置查询模型
 *
 * @author JiYinchuan
 * @since 5.240801.0
 */
@Data
public class InfoCodeConfigQueryValueObject {

    /**
     * 配置列表
     */
    private @Nullable List<@NonNull InfoCodeConfigQueryConfigValueObject> configs;

}
