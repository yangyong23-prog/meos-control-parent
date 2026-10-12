package com.persagy.apccontrol.basic.convert;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDataSourceDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.terminal.AbnormalStatusTypeEnum;
import com.persagy.apccontrol.basic.model.vo.BaseEnumVO;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 基础转换器
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
public interface BaseConvert {

    String INFO_CODE_BUILDING_ID = "buildingId";
    String INFO_CODE_BUILDING_SEQUENCE_ID = "buildingSequenceID";
    String INFO_CODE_FLOOR_ID = "floorId";
    String INFO_CODE_FLOOR_SEQUENCE_ID = "floorSequenceID";
    String INFO_CODE_HEIGHT = "height";
    String INFO_CODE_FLOOR_ELEVATION = "floorElevation";
    String INFO_CODE_TDB = "tdb";
    String INFO_CODE_CO2 = "cO2";
    String INFO_CODE_RH = "rH";
    String INFO_CODE_SERVICE_AREA_TYPE = "serviceAreaType";
    String INFO_CODE_FUNC_TYPE = "funcType";
    String INFO_CODE_AUTO_CTRL_TYPE = "autoCtrlType";
    String INFO_CODE_LAMP_FUNC_TYPE = "lampFuncType";
    String INFO_CODE_ROOM_FUNC_TYPE = "roomFuncType";
    String INFO_CODE_BIM_ID = "bimId";
    String INFO_CODE_BIM_LOCATION = "bimLocation";
    String INFO_CODE_OUTLINE = "outline";
    String INFO_CODE_HAS_ARMING_FUNC = "hasArmingFunc";
    String INFO_CODE_ARMING_SET = "armingSet";
    String INFO_CODE_ARMING_STATUS = "armingStatus";
    String INFO_CODE_CAMERA_TYPE = "cameraType";
    String INFO_CODE_IS_CONTROL = "isControl";
    String INFO_CODE_MANUFACTURER = "manufacturer";
    String INFO_CODE_ACCESS_DEMAND_SET = "accessDemandSet";

    /**
     * 解析String
     */
    BiFunction<Map<String, Object>, String, String> ANALYZE_STRING = MapUtils::getString;

    Function<String, String> DEFAULT_STRING = str -> StringUtils.isBlank(str) ? "-1" : str;

    /**
     * 解析Boolean
     */
    BiFunction<Map<String, Object>, String, Boolean> ANALYZE_BOOLEAN = (map, key) -> {
        if (map != null) {
            final Object answer = map.get(key);
            if (answer != null) {
                if (answer instanceof Boolean) {
                    return (Boolean) answer;
                }
                if (answer instanceof String) {
                    String answerString = (String) answer;
                    if (StringUtils.equals(answerString, "1")) {
                        return true;
                    }
                    if (StringUtils.equals(answerString, "0")) {
                        return false;
                    }
                    return Boolean.valueOf(answerString);
                }
                if (answer instanceof Number) {
                    final Number n = (Number) answer;
                    return n.intValue() != 0 ? Boolean.TRUE : Boolean.FALSE;
                }
            }
        }
        return null;
    };

    /**
     * 解析Integer
     */
    BiFunction<Map<String, Object>, String, Integer> ANALYZE_INTEGER = MapUtils::getInteger;

    /**
     * 解析Integer
     */
    @SuppressWarnings("unchecked")
    BiFunction<Map<String, Object>, String, List<String>> ANALYZE_LIST_STRING = (map, key) ->
            (List<String>) MapUtils.getObject(map, key, Lists.newArrayList());

    /**
     * 解析Integer
     */
    BiFunction<Map<String, Object>, String, BigDecimal> ANALYZE_BIG_DECIMAL = (map, key) -> {
        String stringValue = ANALYZE_STRING.apply(map, key);
        return StringUtils.isNotBlank(stringValue) ? new BigDecimal(stringValue) : null;
    };

    /**
     * 将对象转换为JSON字符串
     */
    Function<Object, String> JSON_WRITE_VALUE_AS_STRING = object -> {
        try {
            return SpringUtil.getBean(ObjectMapper.class).writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    };

    /**
     * 将对象转换为JsonNode
     */
    Function<Object, JsonNode> JSON_WRITE_VALUE_CONVERT_VALUE = object ->
            SpringUtil.getBean(ObjectMapper.class).convertValue(object, JsonNode.class);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.240402.0
     */
    default <E extends BaseEnum<?>> BaseEnumVO toBaseEnumVo(E source) {
        return Objects.nonNull(source) ? new BaseEnumVO(source) : null;
    }

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.240402.0
     */
    default <E extends BaseEnum<?>> List<BaseEnumVO> toBaseEnumVo(Collection<E> source) {
        return Optional.ofNullable(source).orElse(Collections.emptyList()).stream()
                .map(this::toBaseEnumVo).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 转换枚举VO
     *
     * @param classCode    设备类编码
     * @param infoCodeName 信息点名称
     * @param infoCode     信息点
     * @return 枚举VO
     * @since 24Q1 T3S2-5.18.0
     */
    default BaseEnumVO toEnumVo(DtpRwdClassCodeEnum classCode, String infoCodeName, String infoCode) {
        DtpKnowledgeClientService dtpKnowledgeClientService = SpringUtil.getBean(DtpKnowledgeClientService.class);
        Map<String, List<DtpKnowledgeInfoCodeDataSourceDTO>> infoCodeAndDataSources =
                dtpKnowledgeClientService.mapInfoCodeAndDataSources(true, false, classCode.value());
        Map<String, String> infoCodeAndName = Optional.ofNullable(infoCodeAndDataSources.get(infoCodeName))
                .orElseGet(ArrayList::new).stream().collect(Collectors.toMap(DtpKnowledgeInfoCodeDataSourceDTO::getCode,
                        DtpKnowledgeInfoCodeDataSourceDTO::getName, (o1, o2) -> o1));
        return new BaseEnumVO(infoCode, infoCodeAndName.get(infoCode));
    }

    /**
     * 将时间戳转换为LocalDateTime
     *
     * @param timestamp 时间戳
     * @return LocalDateTime
     * @since 7.251001.0
     */
    default LocalDateTime toLocalDateTime(Long timestamp) {
        return DateUtils.toLocalDateTime(timestamp);
    }

    /**
     * 将单个异常状态转换为通用的
     *
     * @param isAbnormal 是否异常
     * @return 通用异常标签
     */
    default List<AbnormalStatusTypeEnum> toCommonTag(Boolean isAbnormal) {
        ArrayList<AbnormalStatusTypeEnum> abnormal = new ArrayList<>();
        if (isAbnormal) {
            abnormal.add(AbnormalStatusTypeEnum.ABNORMAL_EVENT);
        }
        return abnormal;
    }
}
