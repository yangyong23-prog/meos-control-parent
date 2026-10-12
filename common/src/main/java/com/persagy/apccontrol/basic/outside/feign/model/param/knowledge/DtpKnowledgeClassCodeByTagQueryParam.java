package com.persagy.apccontrol.basic.outside.feign.model.param.knowledge;

import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * 数据字段-根据标签查询设备类编码查询参数
 *
 * @author JiYinchuan
 * @since 7.250201.0
 */
@Data
public class DtpKnowledgeClassCodeByTagQueryParam {

    /**
     * 对象类型
     * @see com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum
     */
    private String objType;

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
        private Set<String> tagCode;

    }
}
