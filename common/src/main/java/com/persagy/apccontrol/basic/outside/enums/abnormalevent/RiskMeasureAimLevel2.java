package com.persagy.apccontrol.basic.outside.enums.abnormalevent;

import com.alibaba.fastjson.serializer.JSONSerializable;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.Getter;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * 二级风控目标枚举
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/7/19 14:34
 * @since v5.240502.0
 */
@Getter
public enum RiskMeasureAimLevel2 implements JSONSerializable, BaseEnum<String> {
    E0("E0", 1, "key_2c50c052e4b94b93921ce605077f5724", false, ""),
    E1("E1", 2, "key_f9ee352dc69b49ca89c0b35532c64f66", false, ""),
    E2("E2", 3, "key_0e94e525e65f4d45a719861c15069e13", false, ""),
    E3("E3", 4, "key_7487ffd8269748038b8c16094984b208", false, ""),
    E4("E4", 5, "key_d008205c16a6401482f9f7260db0cccf", false, ""),
    E5("E5", 6, "key_bf012de7d24b498096a1c79592edbf4e", false, ""),
    E6("E6", 7, "key_c1b2f97a369d4728b3947d674d41a6d4", false, ""),
    E7("E7", 8, "key_97b1c34d25b14619ac3ed833a5885283", false, ""),
    E8("E8", 9, "key_f69962914e3448aab09c4255401bb4b3", false, ""),
    E9("E9", 10, "key_0fb1ecaf4d514285a2ad2d49276bb491", false, ""),
    E10("E10", 11, "key_9c31c81feb1e4ab5a35fec94d1cd6206", false, ""),
    E11("E11", 12, "key_2b08caf9f3cb49c4b249638ad984c594", false, ""),
    E12("E12", 13, "key_4a7351da7b784757a904133bfe1557e3", false, ""),
    E13("E13", 14, "key_1cdeab82c9624922aea90de30b23605f", false, ""),
    E14("E14", 15, "key_d5f833d44ac44610a2ea2f828e7caa7e", false, ""),
    E15("E15", 16, "key_1be5de9f554d4eec90c0d64571cceb42", false, ""),
    E16("E16", 17, "key_996f6d9affff4da58b7204aaa3d16980", false, ""),
    E17("E17", 18, "key_6043472245884d01b558ed0b8ec5fed7", false, ""),
    E18("E18", 19, "key_4daf13b68a994d4b82c1f70bb7457dbc", false, ""),
    E19("E19", 20, "key_5dd96e2300c04121a923e25c51d5fc64", false, ""),
    E20("E20", 21, "key_91f157b03e1349a8b4775c09e4489652", false, ""),
    E21("E21", 22, "key_619b7546e92943a5bf588072b6eb7f10", false, ""),
    E22("E22", 23, "key_eb457aaf005d4cb7acc2f42838be75d1", false, ""),
    E23("E23", 24, "key_93b95faebd12402dbc22a932ffaecc19", false, ""),
    E24("E24", 25, "key_ea8f66ecfdcf45fa9f540d30dbe06c79", false, ""),
    E25("E25", 26, "key_7b9bf9c1fe824b60863c07e5a84b8669", false, ""),
    E26("E26", 27, "key_2f3bb4d8f74c468e860bdef3c6ba29b2", false, ""),
    E27("E27", 28, "key_5e63e56558ff4ad8a96191476aa7c0ad", false, ""),
    E28("E28", 29, "key_c6e37513e637440f96a1084ec92c4d3d", false, ""),
    E29("E29", 30, "key_61b7220725c247dc8264e3e25c17d541", false, ""),
    E30("E30", 31, "key_fb568449acf449b0b1b55a37576378fb", false, ""),
    E31("E31", 32, "key_373fc2e865d348ea836d43f01e02a6bf", false, ""),
    E32("E32", 33, "key_d7e199e79d75424ab460e15c0849f117", false, ""),
    E33("E33", 34, "key_2ae1bebe89e44f5181b447eb54724a1a", false, ""),
    E34("E34", 35, "key_d5efabec90a94abeb411beaa49e41e6e", false, ""),
    E35("E35", 36, "key_597b521f490e44beb2eb1ca328e19ccc", false, ""),
    E36("E36", 37, "key_922670b0236741c2ad2746917690f49a", false, ""),
    E37("E37", 38, "key_5b24ee48f2224ec88842194e7d0806da", false, ""),
    E38("E38", 39, "key_de48cce617d947c5b89cbf0e4dc518e8", false, ""),
    E39("E39", 40, "key_5801b1c057884a35ab2a4fdc7a20741f", false, ""),
    E40("E40", 41, "key_990c493dcb284de0bff24948762b1424", false, ""),
    E41("E41", 42, "key_93c215ef22e040fab5f78aaae11f3cc7", false, ""),
    E42("E42", 43, "key_a22a2f434e6545839083f9cdeaf37452", false, ""),
    E43("E43", 44, "key_2aa802836c4645b2815c803d9138f70a", false, ""),
    E44("E44", 45, "key_dbbdda36e3f249d68e738438125cfd09", false, ""),

    ;

    private final String code;
    @EnumValue
    private final int value;
    private final String desc;
    private final boolean isDelete;
    private final String extraValue;


    public void write(JSONSerializer serializer, Object fieldName, Type fieldType, int features) throws IOException {
        serializer.write(this.code);
    }

    public String getEnumCode() {
        return this.code;
    }

    public String getCode() {
        return this.code;
    }

    public int getValue() {
        return this.value;
    }

    public String getDesc() {
        return LocaleMessageUtil.getMessage(this.desc);
    }

    public boolean isDelete() {
        return this.isDelete;
    }

    public String getExtraValue() {
        return this.extraValue;
    }

    private RiskMeasureAimLevel2(final String code, final int value, final String desc, final boolean isDelete, final String extraValue) {
        this.code = code;
        this.value = value;
        this.desc = desc;
        this.isDelete = isDelete;
        this.extraValue = extraValue;
    }

    @Override
    public String value() {
        return code;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(desc);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static RiskMeasureAimLevel2 analyze(String value) {
        return BaseEnum.analyze(value, RiskMeasureAimLevel2.class);
    }
}
