package com.persagy.apccontrol.basic.model.dto.rwd;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.common.DtpRwdPageQuery;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseQueryParamDTO {

    /**
     * 分页参数
     */
    @JsonUnwrapped
    private DtpRwdPageQuery page;

    private List<OrderDTO> orders;
    private BaseQueryPropertyDTO criteria;

}
