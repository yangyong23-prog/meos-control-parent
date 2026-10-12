package com.persagy.apccontrol.basic.model.vo.systemgraph;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.systemgraph.base.simple.SimpleEquipmentVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.annotation.Nonnull;

@ApiModel(value = "MapEquipVo", description = "")
@EqualsAndHashCode(callSuper = true)
@Data
public class MapEquipVo extends SimpleEquipmentVo {
    @Schema(description = "classCode", required = true)
    private String classCode;
    @Schema(description = "bim中模型坐标", required = true)
    private String bimLocation;
    @Schema(description = "", required = true)
    private String bimId;
    @Schema(description = "设备所在空间id", required = true)
    private String spaceId;
    @Schema(description = "所在楼层id", required = true)
    private String floorId;
    @Schema(description = "所在楼层localId", required = true)
    private String floorLocalId;

    @Schema(description = "是否有IOT数据", required = true)
    private Boolean showIot = false;
    @Schema(description = "管道类型", required = true)
    private String specialPipeSection;
    @Schema(description = "是否是管件", required = true)
    private String isPipeFitting;

    @Schema(description = "列表是否隐藏", required = true)
    private boolean hidden = false;
    @Schema(description = "阀门图标数据(isValve=true)，包含中心点和方向向量", required = true)
    private ValveIconDataVo valveIconData;
    @Schema(description = "立管管件的图标数据(isSpecialPipeSection=true)", required = true)
    private PipeIconDataVo pipeIconData;
    @ApiModelProperty("格子信息")
    private EquipmentBoxInfo boxInfo;
    @ApiModelProperty("设备所在格子信息")
    private Integer orderIndex;

    public void copyProperties(@Nonnull DtpRwdObjectDTO rwdObject) {
        setId(rwdObject.getId());
        setLocalName(rwdObject.getLocalName());
        String classCode = rwdObject.getClassCode();
        setClassCode(classCode);
    }

    public static MapEquipVo of(DtpRwdObjectDTO rwdObject) {
        MapEquipVo vo = new MapEquipVo();
        vo.copyProperties(rwdObject);
        return vo;
    }

}
