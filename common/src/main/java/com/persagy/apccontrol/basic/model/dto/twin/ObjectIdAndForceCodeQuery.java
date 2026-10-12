package com.persagy.apccontrol.basic.model.dto.twin;

import cn.hutool.core.collection.CollectionUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 值班首页twin聚合查询请求对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ObjectIdAndForceCodeQuery {

    /**
     * 对象实例Id
     */
    private Set<String> objectIds;
    /**
     * 检查是否为【强制】的信息点
     */
    private Set<String> forceCodes;


    /**
     * 构建分组请求
     *
     * @param group 小卡片名字
     * @param objectIds 小卡片实例Id
     * @param forceCodes 等于条件 手自动状态（manualAutoStatus）=  自动，然后这个设备的信息点上有“强制为8”的指令 的信息点
     * @return 分组对象
     */
    public static Map<String, ObjectIdAndForceCodeQuery> buildDutyHomePageQuery(String group, Set<String> objectIds, Set<String> forceCodes) {
        Map<String, ObjectIdAndForceCodeQuery> map = new HashMap<>();
        if (StringUtils.isEmpty(group) || CollectionUtil.isEmpty(objectIds)) {
            return map;
        }
        ObjectIdAndForceCodeQuery dutyHomePageQuery = new ObjectIdAndForceCodeQuery(objectIds, forceCodes);
        map.put(group, dutyHomePageQuery);
        return map;
    }

    /**
     * 添加多个分组请求
     *
     * @param map
     * @param group
     * @param objectIds
     * @param forceCodes
     */
    public static void addDutyHomePageQuery(Map<String, ObjectIdAndForceCodeQuery> map
            , String group, Set<String> objectIds, Set<String> forceCodes) {
        if (CollectionUtil.isEmpty(map)) {
            return;
        }
        ObjectIdAndForceCodeQuery dutyHomePageQuery = new ObjectIdAndForceCodeQuery(objectIds, forceCodes);
        map.put(group, dutyHomePageQuery);
    }

    public static ObjectIdAndForceCodeQuery getDutyHomePageQuery(Map<String, ObjectIdAndForceCodeQuery> queryParam) {
        if (CollectionUtil.isEmpty(queryParam)) {
            return null;
        }
        return queryParam.values().iterator().next();
    }

}
