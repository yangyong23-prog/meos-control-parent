package com.persagy.apccontrol.basic.model.vo.systemgraph;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.systemgraph.config.GraphInformationVo;
import com.persagy.apccontrol.basic.outside.feign.model.response.knowledge.DtpKnowledgeObjectInfoCodeResponse;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * iot数据信息组装
 *
 * @author houyuanzhi
 * @date 2024/2/20 10:59
 */
@Data
public class IotInfoCodeFieldVo {

    @ApiModelProperty("信息点")
    private String name;
    @ApiModelProperty("信息点编码")
    private String code;
    @ApiModelProperty("单位")
    private String unit;
    @ApiModelProperty("iot数据")
    private String dataValue;
    @ApiModelProperty("iot数据时间")
    private LocalDateTime dataTime;
    @ApiModelProperty("数值占位")
    private String numberPerch;
    @ApiModelProperty("显示模式")
    private Long displayMode;
    @ApiModelProperty("数据源")
    private List<DtpKnowledgeObjectInfoCodeResponse.DataSource> dataSource;
    @ApiModelProperty("信息点类型")
    private String infoType;
    @ApiModelProperty("原始数据类型")
    private String originDataType;
    /**
     * 拼接规则
     */
    @ApiModelProperty("拼接规则")
    private String spliceRule;

    public static IotInfoCodeFieldVo of(InfoCodeValueVO dto, DtpKnowledgeObjectInfoCodeResponse rwdInfo) {
        IotInfoCodeFieldVo vo = new IotInfoCodeFieldVo();
        vo.setCode(dto.getInfoCode());
        if (Objects.nonNull(rwdInfo)) {
            vo.setName(StringUtils.isNotBlank(rwdInfo.getName()) ? rwdInfo.getName() : "");
            vo.setUnit(Objects.equals(rwdInfo.getUnit(), LocaleMessageUtil.getMessage("key_f81cf7864fa24205a6127b27d636f610")) ? null : rwdInfo.getUnit());
            vo.setOriginDataType(rwdInfo.getOrigDataType());
        }
        vo.setDataTime(dto.getUpdateTime());

        Object infoCodeValue = dto.getInfoCodeValue();
        if (Objects.nonNull(infoCodeValue)) {
            Map<String, Object> undefinedProperties = rwdInfo.getUndefinedProperties();
            String dataValue = String.valueOf(dto.getInfoCodeValue());
            if (undefinedProperties.containsKey("scale")) {
                BigDecimal iotValue = new BigDecimal(dataValue);
                vo.setDataValue(String.valueOf(iotValue.setScale(
                        Integer.parseInt(String.valueOf(undefinedProperties.get("scale"))), RoundingMode.HALF_UP)));
            } else {
                vo.setDataValue(dataValue);
            }
        }
        return vo;
    }

    public static IotInfoCodeFieldVo of(InfoCodeValueVO dto, DtpKnowledgeObjectInfoCodeResponse rwdInfo, Map<String, List<GraphInformationVo>> collect) {
        IotInfoCodeFieldVo vo = of(dto, rwdInfo);
        String infoCode = dto.getInfoCode();
        GraphInformationVo graphInformationVo = collect.get(infoCode).get(0);
        vo.setDisplayMode(graphInformationVo.getDisplayMode());
        if (Objects.nonNull(rwdInfo)) {
            List<DtpKnowledgeObjectInfoCodeResponse.DataSource> poiDataSource = rwdInfo.getDataSource();
            vo.setDataSource(CollectionUtils.isNotEmpty(poiDataSource) ? poiDataSource : null);
        }
        boolean b = collect.containsKey(dto.getInfoCode());
        if (StringUtils.isEmpty(vo.getName())) {
            vo.setName(b ? collect.get(dto.getInfoCode()).get(0).getInfoName() : null);
        }
        vo.setInfoType(b ? collect.get(dto.getInfoCode()).get(0).getInfoType() : null);
        return vo;
    }
}
