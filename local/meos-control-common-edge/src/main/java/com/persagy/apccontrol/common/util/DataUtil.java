package com.persagy.apccontrol.common.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName 薛涛
 * @Description 数据处理公共类
 * @Author 薛涛
 * @Date 2024/5/17 14:47
 * @Version 1.0
 */
public class DataUtil {

    /**
     * @Author 薛涛
     * @Description list分页
     * @Date 14:48 2024/5/17
     * @Param [list, pageNo, pageSize]
     * @return java.util.List<T>
     **/
    public static <T> List<T> getPage(List<T> list, int pageNo, int pageSize) {
        int totalSize = list.size();
        int fromIndex = (pageNo - 1) * pageSize;
        if (fromIndex >= totalSize) {
            // 返回空列表
            return new ArrayList<>();
        }
        int toIndex = Math.min(fromIndex + pageSize, totalSize);
        return list.subList(fromIndex, toIndex);
    }
}
