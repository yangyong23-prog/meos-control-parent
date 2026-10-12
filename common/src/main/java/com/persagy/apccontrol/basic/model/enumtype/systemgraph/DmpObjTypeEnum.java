package com.persagy.apccontrol.basic.model.enumtype.systemgraph;

import com.alibaba.fastjson.annotation.JSONCreator;
import com.alibaba.fastjson.serializer.JSONSerializable;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import java.lang.reflect.Type;
import java.util.*;

public enum DmpObjTypeEnum implements JSONSerializable {
    PROJECT("project", "key_c8ff811cb5234f69b587f5dd1fa73141"),
    BUILDING("building", "key_e977b930027f4fcfbf29bdf9ed47663f"),
    FLOOR("floor", "key_860f403627e4419496e3711477912a39"),
    SPACE("space", "key_93930e54e51b4dbda89417183881b172"),
    EQUIPMENT("equipment", "key_b057517385f34aca901cb69c4f64433d"),
    SHAFT("shaft", "key_f4acdd3c2ec34905a6f8e61554a0553c"),
    /**
     * @deprecated
     */
    COMPONENT("component", "key_b8d9aff4c22a4240bc711b1efa8bd66f"),
    MAJOR("major", "key_6e80daf33a6244babfc254251d0d81d2"),
    VIRTUAL("virtual", "key_fb2a1a271fca48319db5cd934fc4f46e"),
    TOOL("tool", "key_2000d842b09949d7a60cefa38ec8ae2c"),
    MATERIAL("material", "key_4170acf519f84f0f85c796696adbb929"),
    SYSTEM("system", "key_1beac127519a46aa8fdb59317ec4a9a3"),
    SHAFT_CLASS("shaftclass", "key_d50790e64be14f93abf94ad1361ace86") {
        public String getDmpValue() {
            return "shaft";
        }

        public DmpObjTypeEnum getObjectEnum() {
            return SHAFT;
        }
    },
    /**
     * @deprecated
     */
    COMPONENT_CLASS("componentclass", "key_84ae1e3f142345e89c365db82df3462d") {
        public String getDmpValue() {
            return "component";
        }

        public DmpObjTypeEnum getObjectEnum() {
            return COMPONENT;
        }
    },
    VIRTUAL_CLASS("virtualclass", "key_cf5d111a9d904863afbe54f79010d838") {
        public String getDmpValue() {
            return "virtual";
        }

        public DmpObjTypeEnum getObjectEnum() {
            return VIRTUAL;
        }
    },
    TOOL_CLASS("toolclass", "key_a7eccdf12864426a93c924122a1f6990") {
        public String getDmpValue() {
            return "tool";
        }

        public DmpObjTypeEnum getObjectEnum() {
            return TOOL;
        }
    },
    MATERIAL_CLASS("materialclass", "key_a605ac4a8d2b4cd99ddf18ec177a156e") {
        public String getDmpValue() {
            return "material";
        }

        public DmpObjTypeEnum getObjectEnum() {
            return MATERIAL;
        }
    },
    FUNCID("funcid", "key_2a72c39ef8064500ab88831d62456481") {
        public DmpObjTypeEnum getObjectEnum() {
            return UNDEFINED;
        }
    },
    BUSINESS_TAG("businessTag", "key_360ca682a70744c9a9fe621b28a49dcd"),
    PIPE("pipe", "key_3c600b69d0704b3db0cffa57de6f4a86"),
    UNDEFINED("undefined", "key_167363685e864e949bc8cee45b5b61c3") {
        public DmpObjTypeEnum getClassEnum() {
            return UNDEFINED;
        }

        public DmpObjTypeEnum getObjectEnum() {
            return UNDEFINED;
        }
    };

    @EnumValue
    private final String value;
    private final String desc;
    private static final Map<String, DmpObjTypeEnum> CACHE = new HashMap();
    private static final String SPACE_CLASS_TYPE = "specificSpace";

    private DmpObjTypeEnum(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public String getValue() {
        return this.value;
    }

    public String getDesc() {
        return LocaleMessageUtil.getMessage(this.desc);
    }

    public static DmpObjTypeEnum convertToObjType(String value) {
        return value == null ? null : (DmpObjTypeEnum) CACHE.get(value);
    }
    public String getBusinessValue() {
        return this.value;
    }

    public String getDmpValue() {
        return this.value;
    }

    public DmpObjTypeEnum getClassEnum() {
        return this;
    }

    public DmpObjTypeEnum getObjectEnum() {
        return this;
    }

    public String getClassType() {
        return this.getClassEnum().getValue();
    }

    @JSONCreator
    public static DmpObjTypeEnum getEnum(String value) {
        DmpObjTypeEnum[] var1 = values();
        int var2 = var1.length;

        for (int var3 = 0; var3 < var2; ++var3) {
            DmpObjTypeEnum item = var1[var3];
            if (item.getValue().equals(value)) {
                return item;
            }
        }

        return UNDEFINED;
    }

    public void write(JSONSerializer serializer, Object fieldName, Type fieldType, int features) {
        serializer.write(this.value);
    }

}
