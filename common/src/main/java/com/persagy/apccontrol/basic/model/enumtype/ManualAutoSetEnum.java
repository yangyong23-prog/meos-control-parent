package com.persagy.apccontrol.basic.model.enumtype;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * 手自动状态，0-人工，1-自动
 * @author yuecaipu
 * @date 2024/8/13 9:47
 * @since v5.240801
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ManualAutoSetEnum implements BaseEnum<Integer>, IEnum<Integer> {

    /**
     * 手自动状态，0-人工，1-自动
     */
    AUTO(1, "key_e31ea221b6e74c5898d5ed6208968a54"),
    MANUAL(0, "key_c0293c3c5576420c9989dd329fd8ce09");


    @EnumValue
    private Integer code;

    private String desc;

    ManualAutoSetEnum(int value,String desc) {
        this.code = value;
        this.desc = desc;
    }

    @Override
    public Integer getValue() {
        return this.code;
    }

    @Override
    public Integer value() {
        return this.code;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(this.desc);
    }

    public String getDesc() {
        return LocaleMessageUtil.getMessage(this.desc);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ManualAutoSetEnum resolve(Integer value) {
        if (value == null) {
            return null;
        }
        return BaseEnum.analyze(value, ManualAutoSetEnum.class);
    }

    public static Map<String, String> getRunningLogTypeMap() {
        ManualAutoSetEnum[] values = ManualAutoSetEnum.values();
        Map<String, String> resultMap = new HashMap<>();
        for (ManualAutoSetEnum runningLogTypeEnum : values) {
            resultMap.put(runningLogTypeEnum.getCode() + "", runningLogTypeEnum.getDesc());
        }
        return  resultMap;
    }

    /**
     * fastjson序列化
     *
     * @author yuecaipu
     * @date 2024/8/13 11:35
     * @since v5.240801
     */

    public static class ManualAutoSetSerializer implements ObjectSerializer {

        public ManualAutoSetSerializer() {
        }
        @Override
        public void write(JSONSerializer jsonSerializer, Object o, Object o1, Type fieldType, int i) throws IOException {
            // 强制把值转换为ManualAutoSetEnum
            ManualAutoSetEnum manualAutoSetEnum = (ManualAutoSetEnum) o;
            // 序列化为自定义的code属性
            jsonSerializer.out.writeInt(manualAutoSetEnum.getCode());
        }
    }

    /**
     * fastjson反序列化
     *
     * @author yuecaipu
     * @date 2024/8/13 11:35
     * @since v5.240801
     */
    public static class ManualAutoSetDeserializer implements ObjectDeserializer {
        public ManualAutoSetDeserializer() {
        }
        @Override
        public <T> T deserialze(DefaultJSONParser parser, Type type, Object o) {
            // 解析值为字符串
            Integer value = parser.parseObject(Integer.class);
            // 遍历所有的枚举实例
            for (ManualAutoSetEnum manualAutoSetEnum : ManualAutoSetEnum.values()) {
                if (manualAutoSetEnum.getCode().equals(value)) {
                    // 成功匹配，返回实例
                    return (T) manualAutoSetEnum;
                }
            }
            // 没有匹配到，可以抛出异常或者返回null
            return null;
        }

        @Override
        public int getFastMatchToken() {
            // 仅仅匹配字符串类型的值
            return JSONToken.LITERAL_INT;
        }
    }
}
