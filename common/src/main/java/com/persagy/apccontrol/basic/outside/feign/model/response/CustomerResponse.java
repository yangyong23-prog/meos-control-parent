package com.persagy.apccontrol.basic.outside.feign.model.response;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.persagy.apccontrol.basic.outside.dto.DmpResult;
import com.persagy.framework.common.enums.ResponseCode;
import com.persagy.framework.common.exception.BusinessException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang.StringUtils;

/**
 * 自定义响应
 *
 * @author JiYinchuan
 * @since v5.0.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CustomerResponse<T> extends BaseResponse {

    /**
     * 响应码
     */
    @JsonAlias({"ResultCode", "respCode", "code"})
    private String resultCode;

    /**
     * 响应结果
     */
    @JsonAlias({"Result", "result", "status"})
    private String result;

    /**
     * 响应信息
     */
    @JsonAlias({"ResultMsg", "respMsg", "reason", "message"})
    private String resultMsg;

    /**
     * 追踪ID
     */
    private String traceId;

    /**
     * 响应内容
     */
    @JsonAlias({"Content", "data", "supportAi"})
    private T content;

    /**
     * 数据条数
     */
    private Integer count;

    /**
     * 数据总条数
     */
    private Integer total;

    /**
     * 原因
     */
    private String cause;

    /**
     * 时间戳
     */
    private Long timestamp;

    public static <T> T handleResult(CustomerResponse<T> result) {
        if (result == null) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_ea44b051491a423288995dce487edad1"));
        }
        String result1 = result.getResult();
        if (StringUtils.isBlank(result1) || !DmpResult.SUCCESS.equals(result1)) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), result.getResultMsg() == null ? LocaleMessageUtil.getMessage("key_92cd29bb9b114e049ec56c1141b79bbd") : result.getResultMsg());
        }
        return result.getContent();
    }

}
