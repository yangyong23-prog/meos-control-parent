package com.persagy.apccontrol.basic.model.enumtype.dtpknowledge;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.config.annotation.InfoCode;
import com.persagy.apccontrol.basic.model.base.BaseInfoCodeValueEnum;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

/**
 * 数据字典-类型编码
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
@InfoCode(InfoCodeEnum.RUN_STATUS)
public enum DtpKnowledgeTypeCodeEnum implements BaseInfoCodeValueEnum<String>, IEnum<String> {

    /**
     * 信息点一级标签
     */
    X1_TAG("x1stTag", "info tag", "一级标签"),

    /**
     * 信息点二级标签
     */
    X2_TAG("x2ndTag", "info tag", "二级标签"),
    /**
     * 多开
     */
    KNOWLEDGE_MAJORS("knowledge_majors", "Major", "专业");

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;

    /**
     * 中文枚举信息
     */
    private final String chineseReasonPhrase;

    @Override
    public String getReasonPhrase() {
        return this.reasonPhrase;
    }

    @Override
    public String value() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static @Nullable DtpKnowledgeTypeCodeEnum analyze(@Nullable Object value) {
        return BaseInfoCodeValueEnum.analyze(value, DtpKnowledgeTypeCodeEnum.class);
    }
}
