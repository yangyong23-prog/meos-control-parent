package com.persagy.apccontrol.basic.model.vo.common;

import com.persagy.apccontrol.basic.model.vo.infocode.InfoCodeObjectGroupVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @ClassName 薛涛
 * @Author Jiangnan Cui
 * @Date 2024/7/23 17:41
 * @Version 1.0
 */
@Data
public class ServiceSpaceBySpaceVO extends InfoCodeObjectGroupVO {

    /**
     * 建筑ID
     */
    private String buildingId;

    /**
     * 建筑本地名称
     */
    private String buildingLocalName;

    /**
     * 楼层ID
     */
    private String floorId;


    /**
     * 楼层本地名称
     */
    private String floorLocalName;
    
    @Schema(description = "楼层顺序")
    private Integer floorSequenceId;

    /**设备服务的空间  此实体类引用别人的不允许修改此实体类*/
    private List<DTPRwdSpaceVO> serviceSpace;
}
