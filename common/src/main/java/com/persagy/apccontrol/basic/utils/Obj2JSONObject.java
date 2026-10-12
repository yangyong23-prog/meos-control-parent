package com.persagy.apccontrol.basic.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.persagy.framework.common.exception.BusinessException;
import org.springframework.lang.NonNull;

import java.util.List;

public class Obj2JSONObject {
    public static JSONObject convert(Object o) throws BusinessException {
        // 为空的字段会被过滤掉
        String jsonStr = JSONObject.toJSONString(o);
        return JSONObject.parseObject(jsonStr);
    }

    public static List<JSONObject> convertList(List<?> list) throws BusinessException {
        // 为空的字段会被过滤掉
        String jsonStr = JSON.toJSONString(list);
        return JSONArray.parseArray(jsonStr, JSONObject.class);
    }

    public static String convert2Str(Object o) {
        // 为空的字段会被过滤掉
        return JSONObject.toJSONString(o);
    }

    public static <T> T reverse(JSONObject jsonObject, Class<T> clazz) {
        return JSONObject.toJavaObject(jsonObject, clazz);
    }

    public static <T> T obj2Class(@NonNull Object object, Class<T> clazz) {
        if (object instanceof JSONObject){
            return reverse((JSONObject) object, clazz);
        }
        return JSONObject.parseObject(JSONObject.toJSONString(object), clazz);
    }
}
