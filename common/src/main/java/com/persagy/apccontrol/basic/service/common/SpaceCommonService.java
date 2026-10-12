package com.persagy.apccontrol.basic.service.common;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdBuildingDTO;
import com.persagy.apccontrol.basic.model.param.common.EquipmentServiceSpaceBySpaceQueryParam;
import com.persagy.apccontrol.basic.model.param.common.EquipmentServiceSpaceQueryParam;
import com.persagy.apccontrol.basic.model.param.common.SensorByServiceSpaceQueryParam;
import com.persagy.apccontrol.basic.model.vo.common.EquipmentServiceSpaceVO;
import com.persagy.apccontrol.basic.model.vo.common.SensorByServiceEquipmentVO;
import com.persagy.apccontrol.basic.model.vo.common.ServiceSpaceBySpaceVO;

import java.util.List;

/**
 * @Author 薛涛
 * @Description 空间公共类
 * @Date 16:10 2024/7/23
 * @Param
 * @return
 **/
public interface SpaceCommonService {

    List<SensorByServiceEquipmentVO> listSensorByServiceSpace(SensorByServiceSpaceQueryParam param);

    List<EquipmentServiceSpaceVO> listEquipmentServiceSpaceByEquipmentId(EquipmentServiceSpaceQueryParam param);

    /**
     * 根据空间ID查询服务空间信息
     *
     * @param param 查询参数
     * @return 服务空间信息
     * @since 5.240702.0
     * @deprecated 该方法已废弃, 内部逻辑不支持根据运行监测二级类型进行查询
     */
    @Deprecated
    List<ServiceSpaceBySpaceVO> listEquipmentServiceSpaceBySpaceId(EquipmentServiceSpaceBySpaceQueryParam param);

    /**
     * 查询某对象所服务的空间树
     *
     * @param groupCode
     * @param projectId
     * @param objectId
     * @return
     */
    List<DtpRwdBuildingDTO> listObjectServiceSpaceByObjectId(String groupCode, String projectId, String objectId);

}
