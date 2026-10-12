package com.persagy.apccontrol.basic.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 键值对VO
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Schema(title = "键值对VO")
public class KeyValueChildrenVO<T extends KeyValueVO> extends KeyValueVO {

    @Schema(description = "键")
    @NotBlank(message = "{key_fa06dcdf32fb4d0aaab58ab3609bc059}")
    private String key;

    @Schema(description = "值")
    @NotBlank(message = "{key_03e78e734620493a85431d08c444f01b}")
    private String value;

    @Schema(description = "子集")
    private List<T> children;

}
