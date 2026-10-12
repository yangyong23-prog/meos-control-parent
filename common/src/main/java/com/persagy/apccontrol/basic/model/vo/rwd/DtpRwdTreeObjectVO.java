package com.persagy.apccontrol.basic.model.vo.rwd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * 物理世界对象树VO
 *
 * @author JiYinchuan
 * @since v5.8.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "物理世界对象树VO")
public class DtpRwdTreeObjectVO extends DtpRwdObjectVO {

    @Schema(description = "子节点")
    private List<DtpRwdTreeObjectVO> children;

}
