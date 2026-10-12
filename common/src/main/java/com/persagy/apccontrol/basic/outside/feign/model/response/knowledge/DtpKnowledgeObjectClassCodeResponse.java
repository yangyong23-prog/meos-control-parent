package com.persagy.apccontrol.basic.outside.feign.model.response.knowledge;

import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 数据字段设备类响应
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DtpKnowledgeObjectClassCodeResponse extends BaseResponse {

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
     * 对象类型
     */
    private String objType;

    /**
     * 对象英文名称
     */
    private String englishName;

}
