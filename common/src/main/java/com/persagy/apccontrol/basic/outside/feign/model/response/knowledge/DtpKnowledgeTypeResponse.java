package com.persagy.apccontrol.basic.outside.feign.model.response.knowledge;

import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 数据字典类型返回响应
 *
 * @author JiYinchuan
 * @since 7.250902.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DtpKnowledgeTypeResponse extends BaseResponse {

    /**
     * 类型ID
     */
    private String typeId;

    /**
     * 类型编码
     */
    private String typeCode;

    /**
     * 编码
     */
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 层级
     */
    private Integer level;

    /**
     * 上级ID
     */
    private String parentId;

    /**
     * 显示顺序
     */
    private Integer showOrder;

}
