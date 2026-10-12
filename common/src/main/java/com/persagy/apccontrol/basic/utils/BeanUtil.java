package com.persagy.apccontrol.basic.utils;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yuyao
 */
public class BeanUtil {

    /**
     * 系统jsonMapper
     */
    public static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    static {
        JSON_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JSON_MAPPER.setDateFormat(new SimpleDateFormat("yyyyMMddHHmmss"));
    }


    public static <E> List<E> jsonMapperCopyProperties(List<?> list, Class<E> target) throws JsonProcessingException {
        JavaType javaType = JSON_MAPPER.getTypeFactory().constructParametricType(List.class, target);
        return JSON_MAPPER.readValue(JSON.toJSONString(list), javaType);
    }


    /**
     * list集合对象属性值拷贝
     *
     * @param source           源list
     * @param destinationClass 目标类CLASS
     * @return return 目标list对象
     */
    public static <E> List<E> listCopyProperties(List<?> source, Class<E> destinationClass) {
        if (source.size() == 0) return new ArrayList<>();
        List<E> res = new ArrayList<E>(source.size());
        for (Object o : source) {
            E e = null;
            e = BeanUtils.instantiateClass(destinationClass);
            BeanUtils.copyProperties(o, e);
            res.add(e);
        }
        return res;
    }

    /**
     * 将list按照一份多少拆分
     * @param inputList  数据
     * @param splitSize  一份几个
     * @return
     * @param <T>
     */
    public static <T> List<List<T>> splitList(List<T> inputList, Integer splitSize) {
        List<List<T>> resultList = new ArrayList<>();
        int listSize = inputList.size();
        int startIndex = 0;
        int endIndex;
        while (startIndex < listSize) {
            endIndex = Math.min(startIndex + splitSize, listSize);
            resultList.add(inputList.subList(startIndex, endIndex));
            startIndex += splitSize;
        }
        return resultList;
    }
}
