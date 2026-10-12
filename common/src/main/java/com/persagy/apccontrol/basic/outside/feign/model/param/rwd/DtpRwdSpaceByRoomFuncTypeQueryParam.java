package com.persagy.apccontrol.basic.outside.feign.model.param.rwd;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.common.DtpRwdPageQuery;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

/**
 * 空间按功能类型查询参数
 *
 * @author JiYinchuan
 * @since 5.240702.0
 */
@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DtpRwdSpaceByRoomFuncTypeQueryParam {

    /**
     * 分页参数
     */
    @JsonUnwrapped
    private DtpRwdPageQuery page;

    /**
     * 空间功能类型编码
     */
    @NotBlank(message = "{key_da2f29612b404d2d9503d93e5ecbadeb}")
    private String roomFuncType;

}
