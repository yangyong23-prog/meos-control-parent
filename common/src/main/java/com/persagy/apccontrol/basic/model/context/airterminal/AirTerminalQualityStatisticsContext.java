package com.persagy.apccontrol.basic.model.context.airterminal;

import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.airterminal.AirTerminalDemandStatusEnum;
import com.persagy.apccontrol.basic.model.param.airterminal.AirTerminalSpaceQualityStatisticsQueryParam;
import com.persagy.apccontrol.basic.model.vo.BaseEnumVO;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.bag.HashBag;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 空调末端-品质统计上下文
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@Data
@Accessors(chain = true)
public class AirTerminalQualityStatisticsContext {

    // ------------------------- 参数 -------------------------

    /**
     * 查询参数
     */
    private AirTerminalSpaceQualityStatisticsQueryParam param;

    // ------------------------- 过程 -------------------------

    /**
     * 温度定义
     */
    private DtpKnowledgeInfoCodeDTO tdbDefine;

    /**
     * 二氧化碳定义
     */
    private DtpKnowledgeInfoCodeDTO co2Define;

    // -------------------- 图例 --------------------

    /**
     * 温度品质区间
     */
    private List<BaseEnumVO> temperatureIntervals = Collections.emptyList();

    /**
     * 二氧化碳品质区间
     */
    private List<BaseEnumVO> carbonDioxideIntervals = Collections.emptyList();

    /**
     * 温度需求状态
     */
    private List<BaseEnumVO> temperatureDemandStatuses = Collections.emptyList();

    /**
     * 二氧化碳需求状态
     */
    private List<BaseEnumVO> carbonDioxideDemandStatuses = Collections.emptyList();

    // -------------------- 空间 --------------------

    /**
     * 空间列表
     */
    private List<DtpRwdSpaceDTO> spaces = Collections.emptyList();

    /**
     * 温度/二氧化碳 -> [空间ID -> 需求状态]
     */
    private Map<String, Map<String, AirTerminalDemandStatusEnum>>
            spaceIdAndDemandStatusBy60Minute = Collections.emptyMap();

    /**
     * 空间ID -> 温度IOT值
     */
    private Map<String, InfoCodeValueVO> spaceIdAndTemperature = Collections.emptyMap();

    /**
     * 空间ID -> 二氧化碳IOT值
     */
    private Map<String, InfoCodeValueVO> spaceIdAndCarbonDioxide = Collections.emptyMap();

    // ------------------------- 结果 -------------------------

    /**
     * 是否处理失败
     */
    private boolean isFailed = false;

    /**
     * 获取温度需求状态包
     *
     * @return 温度需求状态包
     * @since 5.240601.0
     */
    public Bag<AirTerminalDemandStatusEnum> getTemperatureDemandStatusBag() {
        return new HashBag<>(spaceIdAndDemandStatusBy60Minute.getOrDefault(
                DtpRwdSpaceDTO.Fields.tdb, Collections.emptyMap()).values());
    }

    /**
     * 获取二氧化碳需求状态包
     *
     * @return 二氧化碳需求状态包
     * @since 5.240601.0
     */
    public Bag<AirTerminalDemandStatusEnum> getCarbonDioxideDemandStatusBag() {
        return new HashBag<>(spaceIdAndDemandStatusBy60Minute.getOrDefault(
                DtpRwdSpaceDTO.Fields.co2, Collections.emptyMap()).values());
    }

    /**
     * 获取温度值列表
     *
     * @return 温度值列表
     * @since 5.240601.0
     */
    public List<BigDecimal> getTemperatures() {
        return spaceIdAndTemperature.values().stream()
                .filter(item -> Objects.nonNull(item.getInfoCodeValue()))
                .map(item -> new BigDecimal(item.getInfoCodeValue().toString()))
                .collect(Collectors.toList());
    }

    /**
     * 获取二氧化碳值列表
     *
     * @return 二氧化碳值列表
     * @since 5.240601.0
     */
    public List<BigDecimal> getCarbonDioxides() {
        return spaceIdAndCarbonDioxide.values().stream()
                .filter(item -> Objects.nonNull(item.getInfoCodeValue()))
                .map(item -> new BigDecimal(item.getInfoCodeValue().toString()))
                .collect(Collectors.toList());
    }
}
