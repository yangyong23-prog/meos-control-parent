package com.persagy.apccontrol.controller.vo;

import com.alibaba.fastjson.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Schema(title = "登录模块")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginVO {

    @Schema(description = "token")
    private String token;

    @Schema(description = "userName")
    private String userName;

    @Schema(description = "userId")
    private String userId;

    @Schema(description = "项目id")
    private String projectId;


    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "集团编码")
    private String groupCode;

    @Schema(description = "功能菜单,key为functionId,value为resourceId")
    private JSONObject functions;
}
