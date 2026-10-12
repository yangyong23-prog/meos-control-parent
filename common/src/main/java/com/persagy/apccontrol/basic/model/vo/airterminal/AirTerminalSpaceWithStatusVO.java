package com.persagy.apccontrol.basic.model.vo.airterminal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 空调末端-空间带状态VO
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@Data
@Schema(title = "空调末端-空间带状态VO")
public class AirTerminalSpaceWithStatusVO {

    @Schema(description = "空间列表")
    private List<AirTerminalSpaceGraphVO> spaces;

    @Schema(description = "设备列表")
    private List<AirTerminalEquipmentGraphVO> equipments;

    /**
     * 本次空间设备查询取得的原始空间对象。
     *
     * <p>仅供新版缓存构建复用首次 RWD 查询结果，不参与接口序列化。</p>
     *
     * @since v7.260701.0
     */
    @JsonIgnore
    @Schema(hidden = true)
    private List<DtpRwdSpaceDTO> rwdSpaces;

    /**
     * 本次空间设备查询取得的原始设备对象。
     *
     * <p>仅供新版缓存构建复用首次 RWD 查询结果，不参与接口序列化。</p>
     *
     * @since v7.260701.0
     */
    @JsonIgnore
    @Schema(hidden = true)
    private List<DtpRwdEquipmentDTO> rwdEquipments;

}
