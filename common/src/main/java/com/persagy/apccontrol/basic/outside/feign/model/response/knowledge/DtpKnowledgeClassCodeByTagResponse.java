package com.persagy.apccontrol.basic.outside.feign.model.response.knowledge;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 物理世界-根据标签查询设备类参数
 *
 * @author JiYinchuan
 * @since 7.250201.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DtpKnowledgeClassCodeByTagResponse extends BaseResponse {

    /**
     * 类型编码
     */
    @JsonAlias("code")
    private String classCode;

    /**
     * 类型名称
     */
    @JsonAlias("name")
    private String classCodeName;

}
