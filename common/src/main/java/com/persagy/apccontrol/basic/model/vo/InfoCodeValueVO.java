package com.persagy.apccontrol.basic.model.vo;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDefineModel;
import com.persagy.apccontrol.basic.model.dto.ObjectInfoCodeConfigDTO;
import com.persagy.apccontrol.basic.model.enumtype.security.DeploymentStatusEnum;
import com.persagy.apccontrol.basic.model.vo.knowledge.DtpKnowledgeInfoCdeDataSourceVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 动态信息点参数VO
 *
 * @author JiYinchuan
 * @since 5.240402.0
 */
@Data
@Accessors(chain = true)
@Schema(title = "信息点值VO")
public class InfoCodeValueVO {

    @Schema(description = "对象ID")
    private String objectId;

    @Schema(description = "信息点")
    private String infoCode;

    @Schema(description = "信息点名称")
    private String infoCodeName;

    @Nullable
    @Schema(description = "信息点值")
    private Object infoCodeValue;

    @Schema(description = "信息点值")
    private Object dataValue;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "精度")
    private Integer scale;

    @Schema(description = "原始数据类型")
    private String originDataType;

    @Schema(description = "数据时间")
    private LocalDateTime dataTime;

    /**
     * 更新时间
     *
     * @deprecated {@link #dataTime}
     */
    @Nullable
    @Deprecated
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "数据有效期")
    private Integer dataIndate;

    @Nullable
    @Schema(description = "数据过期时间,仅边缘端有")
    private LocalDateTime dataIndicateTime;

    @Nullable
    @Schema(description = "数据源")
    private List<DtpKnowledgeInfoCdeDataSourceVO> dataSources;

    @Schema(description = "拓展值")
    private Map<String, String> additionalMapValue;

    /**
     * 获取数据时间
     *
     * @return 数据时间
     * @since 5.240501.0
     */
    public LocalDateTime getDataTime() {
        return ObjectUtils.defaultIfNull(this.dataTime, this.updateTime);
    }

    /**
     * 转换信息点值为字符串
     *
     * @return 字符串
     * @since 7.250403.0
     */
    public String toDataValueString() {
        return Objects.toString(getInfoCodeValue(), null);
    }

    /**
     * 是否匹配信息点值
     *
     * @param infoCodeValue 信息点值
     * @return 是否匹配
     * @since 5.240502.0
     */
    public boolean equalsInfoCodeValue(@NonNull String infoCodeValue) {
        if (Objects.isNull(this.getInfoCodeValue())) {
            return false;
        }
        return StringUtils.equals(this.getInfoCodeValue().toString(), infoCodeValue);
    }

    /**
     * 数值转空间撤布防状态
     *
     * @return 是否匹配
     * @since 5.240801.0
     */
    public DeploymentStatusEnum valueToDeploymentStatus() {
        if (Objects.isNull(infoCodeValue)) {
            return null;
        }
        return DeploymentStatusEnum.objectToEnum(infoCodeValue);
    }

    /**
     * 填充信息点定义
     *
     * @param defineModel 信息点定义
     * @since 7.260201.0
     */
    public void fillInfoCodeDefine(@NonNull InfoCodeDefineModel defineModel) {
        this.infoCode = defineModel.getCode();
        this.infoCodeName = defineModel.getName();
        this.unit = defineModel.getUnit();
        this.scale = defineModel.getScale();
        this.originDataType = defineModel.getOriginDataType();
    }

    /**
     * 创建信息点值VO
     *
     * @param sources 数据源
     * @return 信息点值VO
     * @since 7.250501.0
     */
    public static List<InfoCodeValueVO> of(@NonNull List<ObjectInfoCodeConfigDTO> sources) {
        if (CollectionUtils.isEmpty(sources)) {
            return Lists.newArrayList();
        }
        List<InfoCodeValueVO> result = new ArrayList<>();
        for (ObjectInfoCodeConfigDTO source : sources) {
            String objectId = source.getObjectId();
            Set<String> infoCodes = source.getInfoCodes();

            if (CollectionUtils.isEmpty(infoCodes)) {
                continue;
            }
            for (String infoCode : infoCodes) {
                InfoCodeValueVO resultItem = new InfoCodeValueVO();
                resultItem.setObjectId(objectId);
                resultItem.setInfoCode(infoCode);
                result.add(resultItem);
            }
        }
        return result;
    }

    /**
     * 转换对象ID -> 时间 -> 数值
     *
     * @param infoCodeValues 信息点数值集合
     * @return 对象ID -> 时间 -> 数值
     * @since 7.260201.0
     */
    public static Map<String, Map<LocalDateTime, String>> mapObjectId2DateTime2Value(
            @Nullable List<InfoCodeValueVO> infoCodeValues) {
        if (CollectionUtils.isEmpty(infoCodeValues)) {
            return Collections.emptyMap();
        }
        return infoCodeValues.stream().collect(Collectors.groupingBy(InfoCodeValueVO::getObjectId,
                Collectors.toMap(InfoCodeValueVO::getDataTime, InfoCodeValueVO::toDataValueString, (v1, v2) -> v2)));
    }

    /**
     * 通用数组转map集合,不区分时间,如果存在相同的时间会进行覆盖
     *
     * @param infoCodeValueList 信息点数值集合
     * @return <信息点,数值>
     */
    public static Map<String, Object> listToInfoCodeValueMap(List<InfoCodeValueVO> infoCodeValueList) {
        Map<String, Object> infoCodeValueMap = new HashMap<>(16);
        if (CollectionUtils.isNotEmpty(infoCodeValueList)) {
            // 按对象id分组后，将信息点编码，信息点数据值再转为数组
            infoCodeValueMap = infoCodeValueList.stream().filter(item -> Objects.nonNull(item.getInfoCodeValue()))
                    .collect(
                            Collectors.toMap(
                                    InfoCodeValueVO::getInfoCode,
                                    InfoCodeValueVO::getInfoCodeValue,
                                    (existing, replacement) -> replacement
                            )
                    );
        }
        return infoCodeValueMap;
    }

    /**
     * 通用数组转map集合,不区分时间,如果存在相同的时间会进行覆盖
     *
     * @param infoCodeValueList 信息点数值集合
     * @return <对象id,<信息点,数值>>
     */
    public static Map<String, Map<String, Object>> listToObjectInfoCodeValueMap(List<InfoCodeValueVO> infoCodeValueList) {
        Map<String, Map<String, Object>> infoCodeValueMap = new HashMap<>(16);
        if (CollectionUtils.isNotEmpty(infoCodeValueList)) {
            // 按对象id分组后，将信息点编码，信息点数据值再转为数组
            infoCodeValueMap = infoCodeValueList.stream()
                    .collect(
                            Collectors.groupingBy(
                                    InfoCodeValueVO::getObjectId,
                                    Collectors.toMap(
                                            InfoCodeValueVO::getInfoCode,
                                            InfoCodeValueVO::getInfoCodeValue,
                                            (existing, replacement) -> replacement
                                    )
                            ));
        }
        return infoCodeValueMap;
    }

    /**
     * 通用数组转map集合，区分时间
     *
     * @param infoCodeValueList 信息点数值集合
     * @return <对象id,<信息点,<时间,数值>>>
     */
    public static Map<String, Map<String, Map<LocalDateTime, Object>>> listToObjectIdInfoCodeTimeValueMap(List<InfoCodeValueVO> infoCodeValueList) {
        Map<String, Map<String, Map<LocalDateTime, Object>>> infoCodeValueMap = new HashMap<>(16);
        if (CollectionUtils.isNotEmpty(infoCodeValueList)) {
            infoCodeValueMap = infoCodeValueList.stream()
                    .collect(Collectors.groupingBy(
                            InfoCodeValueVO::getObjectId,
                            Collectors.groupingBy(
                                    InfoCodeValueVO::getInfoCode,
                                    Collectors.toMap(
                                            InfoCodeValueVO::getUpdateTime,
                                            InfoCodeValueVO::getInfoCodeValue
                                    )
                            )
                    ));
        }
        return infoCodeValueMap;
    }

    /**
     * 通用根据信息点过滤后转成根据对象id分组
     *
     * @param infoCodeValueList 信息点集合
     * @param infoCode          信息点
     * @return <对象id,信息点对象>
     */
    public static Map<String, InfoCodeValueVO> groupInfoCodeValueMap(List<InfoCodeValueVO> infoCodeValueList, String infoCode) {
        return infoCodeValueList.stream()
                .filter(item -> item.getInfoCode().equals(infoCode))
                .collect(Collectors.toMap(
                        InfoCodeValueVO::getObjectId, Function.identity(), (o1, o2) -> o1));
    }

    /**
     * 获取附加Map值，如果为空则创建新的HashMap
     *
     * @return 附加Map值
     * @since 7.250601.0
     */
    public Map<String, String> computeAdditionalMapValueIfAbsent() {
        if (this.additionalMapValue == null) {
            this.additionalMapValue = new HashMap<>();
        }
        return this.additionalMapValue;
    }

    /**
     * 通用数组转map信息点为key，value为对象的map集合 仅限单对象多信息点的情况
     *
     * @param infoCodeValueList 信息点对象
     * @return <信息点,信息点对象>
     */
    public static Map<String, InfoCodeValueVO> groupByInfoCodeAndOneInfoCodeValueMap(List<InfoCodeValueVO> infoCodeValueList) {
        return infoCodeValueList.stream()
                .collect(Collectors.groupingBy(
                        InfoCodeValueVO::getInfoCode, // 分组依据
                        Collectors.collectingAndThen(Collectors.toList(), list -> list.get(0)) // 分组后取第一个元素
                ));
    }
}
