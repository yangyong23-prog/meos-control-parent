package com.persagy.apccontrol.basic.structure.infocodeconfig.model.valueobject;

import com.persagy.apccontrol.basic.model.base.domain.ValueObject;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * 信息点配置查询配置模型
 *
 * @author JiYinchuan
 * @since 5.240801.0
 */
@Data
public class InfoCodeConfigQueryConfigValueObject implements ValueObject {

    /**
     * 分组名称, 配置表中当前配置字段名称, 后续替换为分组编码字段
     *
     * @see #groupCode
     */
    @Deprecated
    private String groupName;

    /**
     * 分组编码
     */
    private String groupCode;

    /**
     * 分组是否深度查询, 默认仅查询当前分组下的信息点
     */
    private @Nullable Boolean deepQuery;

    /**
     * 分组下关联信息点编码
     */
    private @Nullable Set<@NonNull String> infoCodes;

    /**
     * 获取分组编码
     *
     * @return 分组编码
     * @since 5.240501.0
     */
    public @NonNull String getGroupCode() {
        return StringUtils.defaultIfBlank(this.groupCode, this.groupName);
    }
}
