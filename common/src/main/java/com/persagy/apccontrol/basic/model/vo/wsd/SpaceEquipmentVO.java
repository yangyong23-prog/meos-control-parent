package com.persagy.apccontrol.basic.model.vo.wsd;

import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdSpaceVO;
import com.persagy.apccontrol.basic.model.vo.sewage.SewageEquipVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 空间设备信息
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SpaceEquipmentVO extends DtpRwdSpaceVO {

    @Schema(description = "空间下的设备列表")
    private List<SewageEquipVO> equipmentList;
}
