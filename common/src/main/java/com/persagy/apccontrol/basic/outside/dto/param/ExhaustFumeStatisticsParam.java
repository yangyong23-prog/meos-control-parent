package com.persagy.apccontrol.basic.outside.dto.param;

import cn.hutool.core.collection.ListUtil;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 聚合接口 请求参数
 */
@Data
public class ExhaustFumeStatisticsParam {


    //查询对象
    private List<GroupParam> objects;


    //查询时间 默认当前时间 yyyyMMddHHmmss
    private String ipTime = DateUtils.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

    /**
     * 构建查询参数
     *
     * @param param
     */
    public void buildObjects(Map<String, Set<String>> param) {
        objects = new ArrayList<>();

        if (param == null || param.isEmpty()) return;
        param.forEach((k, v) -> {
            GroupParam groupParam = new GroupParam();
            groupParam.setObjectIds(ListUtil.toList(v));
            groupParam.setCode(k);
            objects.add(groupParam);
        });
    }

    @Data
    public static class GroupParam {
        // 对象实例ID
        private List<String> objectIds;
        // 分组编码
        private String code;
    }


    /**
     * 冷热源参数 只有 ID
     */
    @Data
    public static class CoolHeatParam {
        // 对象实例ID
        private List<String> objectIds;
        //查询时间 默认当前时间 yyyyMMddHHmmss
        private String ipTime = DateUtils.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        public CoolHeatParam(List<String> objectIds) {
            this.objectIds = objectIds;
        }
    }

    public void setIpTime(LocalDateTime ipTime) {
        if (ipTime == null) {
            return;
        }
        this.ipTime = ipTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }


}
