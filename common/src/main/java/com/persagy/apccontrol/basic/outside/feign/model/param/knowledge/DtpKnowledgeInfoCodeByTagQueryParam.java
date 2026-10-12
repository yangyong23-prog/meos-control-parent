package com.persagy.apccontrol.basic.outside.feign.model.param.knowledge;

import lombok.Data;

import java.util.Collection;
import java.util.List;

/**
 * 数据字段-根据标签查询信息点查询参数
 *
 * @author wxf
 * @since 7.251202.0
 */
@Data
public class DtpKnowledgeInfoCodeByTagQueryParam {

    /**
     * 设备类
     */
    private Collection<String> classCode;

    /**
     * 标签参数, 多个参数之间为 [且] 的关系
     */
    private List<TagParam> tagParams;

    /**
     * 标签参数
     *
     * @author JiYinchuan
     * @since 7.250201.0
     */
    @Data
    public static class TagParam {

        /**
         * 参数类型
         *
         * <ul>
         *     <li><b>0</b> - 末级</li>
         *     <li><b>1</b> - 枝干</li>
         * </ul>
         */
        private Integer paramType;

        /**
         * 标签类别
         */
        private String typeCode;

        /**
         * 标签值
         */
        private List<String> tagCode;

    }
}
