package com.persagy.apccontrol.basic.outside.feign.model.param.knowledge;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * 数据字典-设备类标签查询参数
 *
 * @author JiYinchuan
 * @since 7.250902.0
 */
@Data
@NoArgsConstructor
public class DtpKnowledgeClassCodeTagQueryParam {

    /**
     * 标签分类名称
     */
    private @Nullable Set<String> tagNames;

    /**
     * 标签值
     */
    private @Nullable Set<String> tagValues;

    /**
     * 对象类编码
     *
     * @see com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum
     */
    private @Nullable Set<String> classCodes;

    /**
     * 对象分类
     *
     * @see com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum
     */
    private @NonNull Set<String> objTypes;

}
