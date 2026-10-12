package com.persagy.apccontrol.basic.model.dto.knowledge;

import com.google.common.collect.Maps;
import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据字典 -> 信息点数据源DTO
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DtpKnowledgeInfoCodeDTO extends BaseResponse {

    @Schema(description = "编码, 全局唯一")
    private String code;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "类型编码")
    private String classCode;

    @Schema(description = "类型名称")
    private String className;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "精度")
    private Integer scale;

    @Schema(description = "一级标签")
    private String firstTag;

    @Schema(description = "二级标签")
    private String secondTag;

    @Schema(description = "原始数据类型")
    private String originDataType;

    @Schema(description = "数据类型")
    private String dataType;

    @Schema(description = "数据源定义")
    private List<DtpKnowledgeInfoCodeDataSourceDTO> dataSources;

    /**
     * 类别 STATIC：静态信息点
     */
    @Schema(description = "类别")
    private String category;

    public static Map<String, String> toDataSourceCodeAndName(List<DtpKnowledgeInfoCodeDataSourceDTO> dataSources) {
        if (CollectionUtils.isEmpty(dataSources)) {
            return Maps.newHashMap();
        }
        return dataSources.stream().collect(Collectors.toMap(DtpKnowledgeInfoCodeDataSourceDTO::getCode,
                DtpKnowledgeInfoCodeDataSourceDTO::getName, (o1, o2) -> o1));
    }
    
    /**
     * 转换数据源编码和名称
     *
     * @return 数据源编码 -> 数据源名称
     * @since 5.240602.0
     */
    public Map<String, String> toDataSourceCodeAndName() {
        return toDataSourceCodeAndName(dataSources);
    }
}
