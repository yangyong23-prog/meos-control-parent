package com.persagy.apccontrol.controller.dto;

import com.persagy.apccontrol.basic.model.dto.SingleObjectDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

/**
 * 联动-单控切换
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/8/15 15:51
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(title = "强控信息点")
public class LinkForceControlDTO extends SingleObjectDTO {

    @NotBlank(message = "{key_8569fcbd6f414eb49b47e35ee077f0c5}")
    @Schema(description = "信息点编码")
    private String infoCode;

    @NotBlank(message = "{key_193b3e05166d4c7cbba6cfafab109560}")
    @Schema(description = "控制模式切换, 0-联动控制，1-单点控制（处于强控）")
    private String model;

}
