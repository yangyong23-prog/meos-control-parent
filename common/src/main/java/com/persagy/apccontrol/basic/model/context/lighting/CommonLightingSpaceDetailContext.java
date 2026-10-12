package com.persagy.apccontrol.basic.model.context.lighting;

import cn.hutool.extra.spring.SpringUtil;
import com.persagy.apccontrol.basic.convert.DtpRwdConvert;
import com.persagy.apccontrol.basic.model.dto.lighting.LightingSpaceTagDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdCircuitDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFloorDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.param.lighting.LightingSpaceDetailQueryParam;
import com.persagy.apccontrol.basic.model.vo.lighting.LightingCircuitDetailVO;
import com.persagy.apccontrol.basic.model.vo.lighting.LightingControllerDetailVO;
import com.persagy.apccontrol.basic.model.vo.lighting.LightingControllerTagVO;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.structure.lighitng.converter.LightingConverter;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 照明-空间详情上下文
 *
 * @author JiYinchuan
 * @since 5.240402.0
 */
@Data
@Accessors(chain = true)
public class CommonLightingSpaceDetailContext {

    // ------------------------- 入参 -------------------------

    /**
     * 查询参数
     */
    private LightingSpaceDetailQueryParam queryParam;

    // ------------------------- 过程 -------------------------

    // -------------------- 过程-基础 --------------------

    /**
     * 空间
     */
    private DtpRwdSpaceDTO space;

    /**
     * 空间标签
     */
    private LightingSpaceTagDTO spaceTag;

    /**
     * 楼层
     */
    private DtpRwdFloorDTO floor;

    // -------------------- 过程-照明回路 --------------------

    /**
     * 照明回路详情上下文
     */
    private CommonLightingCircuitDetailContext circuitDetailContext = new CommonLightingCircuitDetailContext();

    // -------------------- 过程-控制模块 --------------------

    /**
     * 空间下照明回路关联控制模块
     */
    private Collection<DtpRwdObjectDTO> controllers = Collections.emptyList();

    /**
     * 空间下照明回路关联控制模块ID
     */
    private Set<String> controllerIds = Collections.emptySet();

    /**
     * 控制模块ID -> 所在空间
     */
    private Map<String, DtpRwdSpaceDTO> controllerIdAndSpace = Collections.emptyMap();

    /**
     * 控制模块ID -> 所在楼层
     */
    private Map<String, DtpRwdFloorDTO> controllerIdAndFloor = Collections.emptyMap();

    /**
     * 控制模块ID -> 控制模块标签
     */
    private Map<String, LightingControllerTagVO> controllerIdAndTag = Collections.emptyMap();

    /**
     * 控制模块ID -> 关联照明回路
     */
    private Map<String, Set<DtpRwdCircuitDTO>> controllerIdAndCircuits = Collections.emptyMap();

    /**
     * 控制模块ID -> 关联低压末端配电柜
     */
    private Map<String, DtpRwdObjectDTO> controllerIdAndDistributionCabinet = Collections.emptyMap();

    // --------------- 过程-控制模块-异常事件 ---------------

    private Map<String, List<ExceptionEventBaseInfoVO>> controllerIdAndAbnormalEvents = Collections.emptyMap();

    // ------------------------- 结果 -------------------------

    /**
     * 构建照明回路详情
     *
     * @return 照明回路详情
     * @since 5.240402.0
     */
    public List<LightingCircuitDetailVO> buildCircuitDetails() {
        return circuitDetailContext.buildCircuitDetails();
    }

    /**
     * 构建控制模块详情
     *
     * @return 控制模块详情
     * @since 5.240402.0
     */
    public List<LightingControllerDetailVO> buildControllerDetails() {
        DtpRwdConvert dtpRwdConvert = SpringUtil.getBean(DtpRwdConvert.class);
        LightingConverter lightingConverter = SpringUtil.getBean(LightingConverter.class);

        List<LightingControllerDetailVO> result = new ArrayList<>();
        for (DtpRwdObjectDTO controller : controllers) {
            String controllerId = controller.getId();

            LightingControllerTagVO controllerTag = controllerIdAndTag.get(controller.getId());
            List<ExceptionEventBaseInfoVO> exceptionEvents = controllerIdAndAbnormalEvents.get(controller.getId());
            DtpRwdSpaceDTO relSpaceByController = controllerIdAndSpace.get(controllerId);
            DtpRwdFloorDTO relFloorByController = controllerIdAndFloor.get(controllerId);
            Set<DtpRwdCircuitDTO> relCircuits = controllerIdAndCircuits.get(controller.getId());
            DtpRwdObjectDTO relDistributionCabinet = controllerIdAndDistributionCabinet.get(controller.getId());

            List<LightingCircuitDetailVO> relCircuitDetails = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(relCircuits)) {
                Set<String> circuitIds = relCircuits.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
                relCircuitDetails = circuitDetailContext.buildCircuitDetails(circuitIds);
            }

            LightingControllerDetailVO relController = lightingConverter.toControllerDetailVo(controller);
            relController.setTag(controllerTag);
            relController.setAbnormalEvents(exceptionEvents);
            relController.setRelCircuits(relCircuitDetails);
            relController.setRelDistributionCabinet(dtpRwdConvert.toObjectVo(relDistributionCabinet));
            relController.setRelSpace(dtpRwdConvert.toObjectVo(relSpaceByController));
            relController.setRelFloor(dtpRwdConvert.toObjectVo(relFloorByController));
            result.add(relController);
        }
        return result;
    }
}
