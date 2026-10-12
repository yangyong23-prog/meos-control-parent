package com.persagy.apccontrol.basic.outside.feign.model.response.knowledge;

import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * 物理世界对象响应
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DtpKnowledgeObjectInfoCodeResponse extends BaseResponse {

    /**
     * 内容生产工具的版本
     */
    private String version;

    /**
     * 编码, 全局唯一
     */
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 类型编码
     */
    private String classCode;

    /**
     * 类型名称
     */
    private String className;

    /**
     * 单位
     */
    private String unit;

    /**
     * 精度
     */
    private Integer scale;

    /**
     * 一级标签
     */
    private String firstTag;

    /**
     * 二级标签
     */
    private String secondTag;

    /**
     * 原始数据类型
     */
    private String origDataType;

    /**
     * 数据类型
     */
    private String dataType;

    /**
     * 类别
     */
    private String category;

    /**
     * 数据源取值范围/枚举值清单等, 根据dataType有不同的内容
     */
    private List<DataSource> dataSource;

    /**
     * 物理世界数据字段信息点数据源响应
     *
     * @author JiYinchuan
     * @since 24Q1 T3S1-5.17.0
     */
    @Data
    @ToString(callSuper = true)
    @EqualsAndHashCode(callSuper = true)
    public static class DataSource extends BaseResponse {

        /**
         * 编码
         */
        private String code;

        /**
         * 名称
         */
        private String name;

        /**
         * 父级编码
         */
        private String parentCode;

    }
}
