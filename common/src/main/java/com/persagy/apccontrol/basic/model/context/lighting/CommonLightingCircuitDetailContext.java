package com.persagy.apccontrol.basic.model.context.lighting;

import cn.hutool.extra.spring.SpringUtil;
import com.persagy.apccontrol.basic.convert.DtpRwdConvert;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdCircuitDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.lighting.LightingCircuitDetailVO;
import com.persagy.apccontrol.basic.model.vo.lighting.LightingCircuitTagVO;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdCircuitVO;
import com.persagy.apccontrol.basic.outside.dto.IotDataCommon;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.lighitng.converter.LightingConverter;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 照明-照明回路详情上下文
 *
 * @author JiYinchuan
 * @since 5.240402.0
 */
@Data
@Accessors(chain = true)
public class CommonLightingCircuitDetailContext {

    // ------------------------- 入参 -------------------------

    /**
     * 项目ID
     */
    private String projectId;

    /**
     * 模版类型
     */
    private DemandTemplateTypeEnum templateType;

    /**
     * 照明回路
     */
    private Collection<DtpRwdCircuitDTO> circuits = Collections.emptyList();

    // ------------------------- 过程 -------------------------

    // --------------- 过程-基础 ---------------

    /**
     * 照明回路ID -> 照明回路标签
     */
    private Map<String, LightingCircuitTagVO> circuitIdAndTag = Collections.emptyMap();

    /**
     * 照明回路ID -> 关联电井
     */
    private Map<String, DtpRwdSpaceDTO> circuitIdAndElectricWel = Collections.emptyMap();

    /**
     * 照明回路ID -> 关联控制模块
     */
    private Map<String, DtpRwdObjectDTO> circuitIdAndController = Collections.emptyMap();

    /**
     * 照明回路ID -> 关联低压末端配电柜
     */
    private Map<String, DtpRwdObjectDTO> circuitIdAndDistributionCabinet = Collections.emptyMap();

    // --------------- 过程-异常事件 ---------------

    private Map<String, List<ExceptionEventBaseInfoVO>> circuitIdAndAbnormalEvents = Collections.emptyMap();

    // --------------- 过程-动态信息点参数 ---------------

    /**
     * 照明回路运行信息点配置
     */
    private List<RcInfoCodeCfg> circuitRunningInfoCodes = Collections.emptyList();

    /**
     * 照明回路ID -> IOT运行信息点反馈信息
     */
    private Map<String, List<IotDataCommon>> circuitIdAndRunningFeedbackInfos = Collections.emptyMap();
    private Map<String, List<IotDataCommon>> circuitIdAndRunningInfos= Collections.emptyMap();

    /**
     * 照明回路ID -> 运行信息点参数
     */
    private Map<String, List<InfoCodeValueVO>> circuitIdAndRunningInfoCodeParameters = Collections.emptyMap();

    /**
     * 照明回路控制信息点配置
     */
    private List<RcInfoCodeCfg> circuitControlInfoCodes = Collections.emptyList();

    /**
     * 照明回路ID -> IOT控制信息点反馈信息
     */
    private Map<String, List<IotDataCommon>> circuitIdAndControlFeedbackInfos = Collections.emptyMap();

    /**
     * 照明回路ID -> 控制信息点参数
     */
    private Map<String, List<InfoCodeValueVO>> circuitIdAndControlInfoCodeParameters = Collections.emptyMap();

    /**
     * 填充判断参数信息点
     */
    private Map<String, IotDataCommon> restrictInfos;

    /**
     * 查询回路绑点信息
     */
    private Set<String> ltltccExistRulesSet = new HashSet<>();

    // ------------------------- 结果 -------------------------

    /**
     * 构建照明回路详情
     *
     * @return 照明回路详情
     * @since 5.240402.0
     */
    public List<LightingCircuitDetailVO> buildCircuitDetails() {
        return buildCircuitDetails(null);
    }

    /**
     * 构建照明回路详情
     *
     * @param filterCircuitIds 过滤照明回路ID, 未空时构建所有数据
     * @return 照明回路详情
     * @since 5.240402.0
     */
    public List<LightingCircuitDetailVO> buildCircuitDetails(@Nullable Collection<String> filterCircuitIds) {
        List<DtpRwdCircuitDTO> eachCircuits = new ArrayList<>(circuits);
        if (CollectionUtils.isNotEmpty(filterCircuitIds)) {
            eachCircuits = eachCircuits.stream()
                    .filter(circuit -> filterCircuitIds.contains(circuit.getId()))
                    .collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(eachCircuits)) {
            return Lists.newArrayList();
        }

        DtpRwdConvert dtpRwdConvert = SpringUtil.getBean(DtpRwdConvert.class);
        LightingConverter lightingConverter = SpringUtil.getBean(LightingConverter.class);

        List<DtpRwdCircuitVO> circuitVos = dtpRwdConvert.toCircuitVo(eachCircuits);

        List<LightingCircuitDetailVO> result = new ArrayList<>();
        for (DtpRwdCircuitVO circuit : circuitVos) {
            String circuitId = circuit.getId();

            DtpRwdSpaceDTO relElectricWel = circuitIdAndElectricWel.get(circuitId);

            LightingCircuitTagVO circuitTag = circuitIdAndTag.get(circuitId);
            List<InfoCodeValueVO> runningInfoCodeParameters = circuitIdAndRunningInfoCodeParameters.get(circuitId);
            List<InfoCodeValueVO> controlInfoCodeParameters = circuitIdAndControlInfoCodeParameters.get(circuitId);
            List<ExceptionEventBaseInfoVO> abnormalEvents = circuitIdAndAbnormalEvents.get(circuitId);
            DtpRwdObjectDTO relController = circuitIdAndController.get(circuitId);
            DtpRwdObjectDTO relDistributionCabinet = circuitIdAndDistributionCabinet.get(circuitId);

            LightingCircuitDetailVO relCircuit = lightingConverter.toCircuitDetailVo(circuit);
            relCircuit.setTag(circuitTag);
            relCircuit.setRunningParameters(runningInfoCodeParameters);
            relCircuit.setSettingParameters(controlInfoCodeParameters);
            relCircuit.setAbnormalEvents(abnormalEvents);
            relCircuit.setRelController(dtpRwdConvert.toObjectVo(relController));
            relCircuit.setRelDistributionCabinet(dtpRwdConvert.toObjectVo(relDistributionCabinet));
            relCircuit.setRelSpace(dtpRwdConvert.toObjectVo(relElectricWel));
            relCircuit.setRestrictInfos(getRestrictInfos(circuitId));
            result.add(relCircuit);
        }
        return result;
    }


    /**
     * 获取照明回路的现在信息点
     * 在线状态 本地远程状态
     * key信息点,val 信息点值 未交付 返回 NULL, 无数据 返回-1
     *
     * @return val限制信息点
     */
    private Map<String, String> getRestrictInfos(String id) {

        Map<String, String> map = new HashMap<>();

        //获取对象的本地远程信息

        IotDataCommon localRemoteStatus = restrictInfos.get(id + InfoCodeEnum.LOCAL_REMOTZE_STATUS.getCode());
        IotDataCommon onlineStatus = restrictInfos.get(id + InfoCodeEnum.ONLINE_STATUS.getCode());

        //先判断是否有绑点信息
        if (!ltltccExistRulesSet.contains(id + InfoCodeEnum.LOCAL_REMOTZE_STATUS.getCode())) {
            map.put(InfoCodeEnum.LOCAL_REMOTZE_STATUS.getCode(), null);
        } else {
            map.put(InfoCodeEnum.LOCAL_REMOTZE_STATUS.getCode(), localRemoteStatus == null || localRemoteStatus.getDataValue() == null
                    ? "-1" : localRemoteStatus.getDataValue());
        }
        if (!ltltccExistRulesSet.contains(id + InfoCodeEnum.ONLINE_STATUS.getCode())) {
            map.put(InfoCodeEnum.ONLINE_STATUS.getCode(), null);
        } else {
            map.put(InfoCodeEnum.ONLINE_STATUS.getCode(), onlineStatus == null || onlineStatus.getDataValue() == null
                    ? "-1" : onlineStatus.getDataValue());
        }
        return map;
    }
}
