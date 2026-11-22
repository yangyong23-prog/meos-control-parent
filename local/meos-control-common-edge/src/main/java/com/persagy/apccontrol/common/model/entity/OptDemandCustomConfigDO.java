package com.persagy.apccontrol.common.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 运行标准过渡 B1：dtp-meos {@code demand_custom_config} 的只读映射（仅判定所需字段）。
 *
 * <p>该表归 dtp-meos，我方只读不写。存在本实体只是为了让 {@link com.persagy.apccontrol.common.mapper.OptStandardsModeMapper}
 * 带上实体泛型：框架的分表拦截器（DynamicPartitionTableNameInnerInterceptor）对每条语句都会按 Mapper 泛型解析实体类，
 * 没有泛型会报“[分表策略] 获取实体类失败”并使查询失败。
 *
 * @since opt/standards-transition
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("demand_custom_config")
public class OptDemandCustomConfigDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 项目 ID */
    private String projectId;

    /** 对象 ID（项目级配置时等于项目 ID） */
    private String objectId;

    /** 模块编码，判定时为 CALCULATE */
    private String moduleCode;

    /** 配置类型，判定时为 STANDRAD_CONFIG（dtp-meos 原拼写） */
    private String configType;
}
