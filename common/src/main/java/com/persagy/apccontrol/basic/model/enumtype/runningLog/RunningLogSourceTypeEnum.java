package com.persagy.apccontrol.basic.model.enumtype.runningLog;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 运行日志来源类型枚举
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum RunningLogSourceTypeEnum implements BaseEnum<String>, IEnum<String> {


    /**
     * dtp-meos 来源消息
     */
    SCHEDULE("SCHEDULE", "dtp-meos", "key_2025d3e1fd7342e8a8546bc4ced3ee37"),

    COMMAND("COMMAND", "dtp-meos", "key_557f1860644647b3919888d154dab121"),

    INTERVENTION("INTERVENTION", "dtp-meos", "key_4ed16dba79c64426ac0c8a792af4126f"),

    CHANGE_INTERVENTION_TIME("CHANGE_INTERVENTION_TIME", "dtp-meos", "key_26bd18d9c2404a4e851be30d35aaa5e5"),


    /**
     * twin 服务来源消息
     */
    TWIN_COMMAND("TWIN_COMMAND", "dtp-twin", "key_b7eaf0e77f324bd1a21eaba16d9c663d"),


    /**
     * meos 自己的服务来源消息
     */
    MEOS_LIGHTING_INTERVENTION_TASK("MEOS_LIGHTING_INTERVENTION_TASK", "meos", "key_5b2d55ccbcfe4b5c96073b1c4e46fec7"),
    MEOS_AIR_TERMINAL_INTERVENTION_TASK("MEOS_AIR_TERMINAL_INTERVENTION_TASK", "meos", "key_5b2d55ccbcfe4b5c96073b1c4e46fec6"),
    ;


    @EnumValue
    private String code;

    // 来源服务
    private String serve;

    private String desc;


    //判断是否是 twin 服务类型
    public static boolean isTwin(RunningLogSourceTypeEnum type) {
        return RunningLogSourceTypeEnum.TWIN_COMMAND.equals(type);
    }

    public static boolean isMeos(RunningLogSourceTypeEnum type) {
        return MEOS_LIGHTING_INTERVENTION_TASK.equals(type) ||
                MEOS_AIR_TERMINAL_INTERVENTION_TASK.equals(type);
    }

    //判断是否是 dtp-meos 服务类型
    public static boolean isDtpMeos(RunningLogSourceTypeEnum type) {
        return SCHEDULE.equals(type) ||
                COMMAND.equals(type) ||
                CHANGE_INTERVENTION_TIME.equals(type) ||
                INTERVENTION.equals(type);
    }


    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static RunningLogSourceTypeEnum resolve(String value) {
        if (value == null) {
            return null;
        }
        return BaseEnum.analyze(value, RunningLogSourceTypeEnum.class);
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
        return LocaleMessageUtil.getMessage(this.desc);
    }

    public String getDesc() {
        return LocaleMessageUtil.getMessage(this.desc);
    }

}
