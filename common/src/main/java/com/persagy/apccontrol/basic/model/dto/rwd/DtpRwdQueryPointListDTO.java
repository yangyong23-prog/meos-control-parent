package com.persagy.apccontrol.basic.model.dto.rwd;

import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 分精度规则，目前仅需要信息点，返回则代表绑点，其他详见文档
 * http://172.19.0.143:9099/project/116/interface/api/35604
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2025/1/22 15:05
 * @since 7.250102.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(title = "分精度规则")
@ToString(callSuper = true)
public class DtpRwdQueryPointListDTO extends BaseResponse {

    @Schema(title = "信息点,如果返回则为已绑点,没返回则未绑点")
    private String infoCode;

}
