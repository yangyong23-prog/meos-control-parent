package com.persagy.apccontrol.basic.model.vo.duty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Collection;

/**
 * 安防卡片数据
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2025/4/24 15:06
 * @since 7.250402.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SecurityCardDataVO extends BasicsCardVO {

    @Schema(description = "子卡片数据")
    private Collection<SecurityChildCardDataVO> listSecurityChildCard;

    @Schema(description = "卡片标签数据")
    private CardTagVO cardTag;

}
