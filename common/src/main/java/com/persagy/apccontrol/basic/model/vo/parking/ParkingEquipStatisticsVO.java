package com.persagy.apccontrol.basic.model.vo.parking;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ParkingEquipStatisticsVO {

    @Schema(description = "建筑id")
    private int count;

    @Schema(description = "对象编码")
    private String objectCode;

    @Schema(description = "对象名")
    private String objectName;

    @Schema(description = "状态列表")
    private List<ParkingEquipStatusStatisticsVO> statusList = new ArrayList<>();


//   void initDataStatusList(){
//       ParkingEquipStatusStatisticsVO exceptionVo = new ParkingEquipStatusStatisticsVO();
//       exceptionVo.setObjectCode(SecurityEquipmentStatusEnum.EXCEPTION);
//       statusList.add(exceptionVo);
//       ParkingEquipStatusStatisticsVO offlineVo = new ParkingEquipStatusStatisticsVO();
//       offlineVo.setObjectCode(SecurityEquipmentStatusEnum.OFFLINE);
//       statusList.add(offlineVo);
//    }
}
