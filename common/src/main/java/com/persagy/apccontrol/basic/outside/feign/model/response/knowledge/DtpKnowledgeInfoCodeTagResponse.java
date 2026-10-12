package com.persagy.apccontrol.basic.outside.feign.model.response.knowledge;

import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * 数据字典信息点标签返回响应
 *
 * @author wxf
 * @since 7.251202.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DtpKnowledgeInfoCodeTagResponse extends BaseResponse {

    /**
     * 标签列表
    **/
    Map<String, List<String>> tags;

    /**
     * infocode
     **/
    String code;

    /**
     * infocode 名
     **/
    String name;

    /**
     * classCode
     **/
    String classCode;
}
