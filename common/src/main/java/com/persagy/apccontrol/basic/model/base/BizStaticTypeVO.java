package com.persagy.apccontrol.basic.model.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * @Author : yuyao
 * @Date : 2022/3/21 11:24 AM
 * @Description ：
 */
@Schema(title = "枚举视图")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@AllArgsConstructor
@Builder
public class BizStaticTypeVO extends DomainBase {

    /**
     * 编码
     */
    @Schema(description = "编码")
    private String code;

    /**
     * 描述
     */
    @Schema(description = "描述")
    private String explain;


}
