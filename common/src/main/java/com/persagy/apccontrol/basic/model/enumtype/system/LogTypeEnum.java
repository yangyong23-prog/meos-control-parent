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
 * @Description: 日志业务类型
 * @author: yancheng
 * @date: 2023/3/16
 * @version: V1.0
 */
@Getter
@AllArgsConstructor
public enum LogTypeEnum implements BaseEnum<String>, IEnum<String> {

    COMMAND_SEND("command_send", "key_fc03cbb7eed144fd995447fec98df6b8"),

    /**
     *
     */
    TASK("task", "key_32803948ad3149b6a2f0fd393a3695fd"),
    RUN_ADJUST("run_adjust", "key_053d4a034531482ebaa7e8fbeeffb05f"),
    RUN_ADJUST_AUTOCONTROL("run_adjust_automatic", "key_bbd420d07d844ff088860faed7435df2"),
    RUN_ADJUST_ENGINEER("run_adjust_engineer", "key_906106140d894c80a3ea25f9cfd5aade"),
    DEMAND_CHANGE("demand_change", "key_5453a578bce349759ac2cf1a1f697916"),
    SYSTEM_OPERATION("system_operation", "key_dfb9fd4db2ac4ec2b6a9cb4221c034b2"),
    LOG_IN("log_in", "登录日志"),
    PAGE_BROADCASR("page_broadcast", "页面播报"),

    ;
    private final String code;
    private final String desc;


    /**
     * 通过CODE 获取枚举
     *
     * @param code
     * @return
     */
    public static LogTypeEnum getEnumByCode(String code) {
        if (code == null) {
            return null;
        }
        for (LogTypeEnum i : LogTypeEnum.values()) {
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

        for (LogTypeEnum i : LogTypeEnum.values()) {
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
