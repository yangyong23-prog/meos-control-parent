package com.persagy.apccontrol.basic.outside.feign.model.response.knowledge;

import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 数据字典设备类标签响应
 *
 * @author JiYinchuan
 * @since 7.250902.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DtpKnowledgeClassCodeTagResponse extends BaseResponse {

    /**
     * 类别编码
     */
    private String classCode;

    /**
     * 标签主体ID
     */
    private String subjectId;

    /**
     * 标签主体类别
     */
    private String subject;

    /**
     * 标签类型
     */
    private String tagType;

    /**
     * 标签名称
     */
    private String tagTypName;

    /**
     * 标签值
     */
    private String tagValue;

    /**
     * 标签值名称
     */
    private String tagValueName;

    /**
     * 是否在服务范围
     *
     * <ul>
     *     <li><b>0</b> - 否</li>
     *     <li><b>1</b> - 是</li>
     * </ul>
     */
    private Integer inScope;

}
