package com.persagy.apccontrol.basic.model.base;

import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author yuyao
 * @Date 2022/3/2 2:05 PM
 * @Description
 */
@Getter
public enum EnumYesNo implements BaseEnum<String>, IEnum<String> {

    /**
     * 状态枚举
     */
    YES("y", "key_42bb8d323c484d7fa6be700b8d5f55fd", 1),
    NO("n", "key_f0173c0934b2474cb4d59ed1ee2150e3", 0);

    /**
     * 编码
     */
    private final String code;

    /**
     * 描述
     */
    private final String explain;
    /**
     * 数字编码
     */
    private final Integer num;

    EnumYesNo(String code, String explain, Integer num) {
        this.code = code;
        this.explain = explain;
        this.num = num;
    }

    public static EnumYesNo getEnumByCode(String code) {
        for (EnumYesNo i : EnumYesNo.values()) {
            if (code.equals(i.getCode())) {
                return i;
            }
        }
        return null;
    }

    public static List<BizStaticTypeVO> getList() {

        List<BizStaticTypeVO> list = ListUtil.toList();

        for (EnumYesNo i : EnumYesNo.values()) {
            list.add(new BizStaticTypeVO(String.valueOf(i.getCode()), i.getExplain()));
        }
        return list;
    }

    @Override
    public String getValue() {
        return LocaleMessageUtil.getMessage(this.explain);
    }

    /**
     * 获取字符串类型code
     *
     * @param
     * @author yuyao
     * @date 2022/7/12 09:48
     * @Return java.lang.String
     */
    public String getCodeByString() {
        return String.valueOf(this.code);
    }

    @Override
    public String value() {
        return LocaleMessageUtil.getMessage(this.explain);
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(this.explain);
    }
    public String getExplain() {
        return LocaleMessageUtil.getMessage(this.explain);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static EnumYesNo resolve(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        return BaseEnum.analyze(value, EnumYesNo.class);
    }
}
