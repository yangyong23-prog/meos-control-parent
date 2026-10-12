package com.persagy.apccontrol.basic.config;

import lombok.Data;
import org.apache.commons.compress.utils.Lists;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 弱电系统关系配置
 * 支持动态配置设备之间的关系映射
 *
 * @author weiyizhong
 * @since 7.251202.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "weak-power")
@RefreshScope
public class WeakPowerRelationConfig {

    /**
     * 最大层级深度限制
     */
    private int maxHierarchyDepth = 10;


    /**
     * 关系数据
     */
    private List<Relation> relations = Lists.newArrayList();


    /**
     * 中间设备类型（需要继续查询的设备）
     */
    private List<String> intermediateDeviceTypes;

    @Data
    public static class Relation {
        /** 主对象名称 */
        private String master;
        /** 主对象编码 */
        private String masterCode;
        /** 图类型编码 */
        private String graphType;
        /** 边类型编码 */
        private String edgeType;
        /** to端可能出现的类型列表 */
        private List<String> slaveTypes;
        /** 备注 */
        private String remark;
    }


    /**
     * 工具方法：根据主对象编码拿到关系数据
     */
    public List<Relation> getRelationByMasterCode(String masterCode) {
        return relations.stream()
                .filter(r -> masterCode.equalsIgnoreCase(r.getMasterCode()))
                .collect(Collectors.toList());
    }

}