package com.persagy.apccontrol.basic.outside.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import lombok.Data;
import lombok.ToString;

/**
 * @author yuyao
 * @date 2022/8/23 11:26
 * 对象绑定信息点信息
 */
@Data
@ToString(callSuper = true)
public class ObjectsExistRule extends BaseResponse {

    @JsonProperty("objectId")
    private String objectId;

    @JsonProperty("classCode")
    private String classCode;

    @JsonProperty("infoCode")
    private String infoCode;

}
