package com.persagy.apccontrol.basic.structure.infocodeconfig.model.dto;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdBuildingDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFloorDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.vo.infocode.InfoCodeGroupVO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * 信息点配置-物理世界对象详情值对象
 *
 * @author JiYinchuan
 * @since 5.240801.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class InfoCodeRwdObjectDetailDTO extends DtpRwdObjectDTO {

    /**
     * 关联信息点分组配置
     */
    private List<InfoCodeGroupVO> relInfoCodeGroupConfigs;

    /**
     * 关联对象
     */
    private List<InfoCodeRwdObjectDetailDTO> relObjects;

    /**
     * 所在空间
     */
    private List<DtpRwdSpaceDTO> inSpaces;

    /**
     * 所在楼层
     */
    private DtpRwdFloorDTO inFloor;

    /**
     * 所在的建筑
     */
    private DtpRwdBuildingDTO inBuilding;

}
