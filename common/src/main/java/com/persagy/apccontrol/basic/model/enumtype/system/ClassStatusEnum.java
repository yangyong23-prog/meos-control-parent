package com.persagy.apccontrol.basic.model.enumtype.system;

import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.persagy.apccontrol.basic.model.base.BizStaticTypeVO;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * @Description: 设备状态
 * @author: yancheng
 * @date: 2023/3/17
 * @version: V1.0
 */
@Getter
@AllArgsConstructor
public enum ClassStatusEnum implements BaseEnum<String>, IEnum<String> {
    /**
     *
     */
    NORMAL("normal", "key_e9d200d236e64b8584fb926b8aadddc4"),
    OPEN("open", "key_5f6a4957123248279ddf22f6cfa74063"),
    CLOSE("close", "key_b1620633958840638440dd1b3e10024e"),
    WAIT_CLOSE("wait_close", "key_88e969f6b89d44d38291d63b91073153"),
    WAIT_OPEN("wait_open", "key_d872e693bd7449d684be8ff4bddd9600"),
    OFF_LINE("off_line", "key_dbb8370e33304ba1adf0d4462ab73be2"),
    ;
    private final String code;
    private final String desc;


    /**
     * 通过CODE 获取枚举
     *
     * @param code
     * @return
     */
    public static ClassStatusEnum getEnumByCode(String code) {
        if (code == null) {
            return null;
        }
        for (ClassStatusEnum i : ClassStatusEnum.values()) {
            if (code.equals(i.getCode())) {
                return i;
            }
        }
        return null;
    }

    /**
     * 获取枚举值列表
     *
     * @return KEY VAL 数据列表
     */
    public static List<BizStaticTypeVO> getList() {

        List<BizStaticTypeVO> list = ListUtil.toList();

        for (ClassStatusEnum i : ClassStatusEnum.values()) {
            list.add(new BizStaticTypeVO(String.valueOf(i.getCode()), i.getDesc()));
        }
        return list;
    }

    @Override
    public String getValue() {
        return this.code;
    }

    @Override
    public String value() {
        return this.code;
    }

    @Override
    public String getReasonPhrase() {
        return this.getDesc();
    }

    public String getDesc() {
        return LocaleMessageUtil.getMessage(this.desc);
    }
}
