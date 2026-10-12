package com.persagy.apccontrol.basic.model.vo;

import cn.hutool.http.Method;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * meoi-aim-server 接口转发实体类
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/2/20 17:25
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CommonServerForwardVO {

    @Schema(description = "项目ID")
    private String groupCode;

    @NotBlank(message = "{key_79ae698d8ca34f22a0784247c6543796}")
    @Schema(description = "项目ID")
    private String projectId;

    @NotNull(message = "{key_040e0189c5db4225b07d49e948b2b7fd}")
    @Schema(description = "接口方式")
    private Method method;

    @NotBlank(message = "{key_ce8a440b712640429abc04e9b97742d2}")
    @Schema(description = "转发地址")
    private String uri;

    @Schema(description = "数据类型")
    private String contentType;

    @Schema(description = "请求体")
    private Object body;

    @Schema(description = "GET参数")
    private Map<String, Object> param;

}
