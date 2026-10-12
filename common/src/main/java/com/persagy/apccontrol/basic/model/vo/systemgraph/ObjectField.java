package com.persagy.apccontrol.basic.model.vo.systemgraph;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.utils.systemgraph.FieldUtils;
import com.persagy.apccontrol.basic.utils.systemgraph.SafeDateUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 对象信息
 *
 * @author chenlong
 * @date 2022/1/18 3:30 下午
 */
@Data
@Accessors(chain = true)
@ApiModel(description = "对象字段")
@Slf4j
public class ObjectField {
    @ApiModelProperty("标签")
    private String label;
    @ApiModelProperty("原始值")
    private Object rawValue;
    @Schema(description = "信息点元数据")
    private @Nullable
    FieldMeta fieldMeta;
    @ApiModelProperty("序号")
    private Integer order;
    @Schema(description = "字段是否可编辑", required = true)
    private boolean editable;

    @ApiModelProperty("显示形式")
    public DisplayMode getType() {
        if (fieldMeta == null) {
            return DisplayMode.TEXT;
        }
        if (Lists.newArrayList(FieldMeta.FieldMetaDataType.ATTACHMENT,
                FieldMeta.FieldMetaDataType.PICTURE,
                FieldMeta.FieldMetaDataType.POSITION_MAP,
                FieldMeta.FieldMetaDataType.SIMPLE_OBJECT).contains(fieldMeta.getDataType())) {
            return DisplayMode.BLOB;
        }
        return DisplayMode.TEXT;
    }

    @ApiModelProperty("显示值")
    public String getShowText() {
        if (fieldMeta == null) {
            return parseShowText(rawValue);
        }
        if (rawValue == null) {
            return null;
        }
        if (rawValue instanceof String && StringUtils.isBlank(rawValue.toString())) {
            return null;
        }
        if (rawValue instanceof Collection && CollectionUtils.isEmpty((Collection<?>) rawValue)) {
            return null;
        }
        //如果是附件,就什么都不显示
        if (getType() != DisplayMode.TEXT) {
            return null;
        }
        // 如果rawValue本身是多个，但是信息点定义不是多个，也当做多个处理
        if (fieldMeta.isMultiple() || rawValue instanceof Collection) {
            if (!(rawValue instanceof JSONArray)) {
                // 适配一次字符串，因为有很多不正确的信息点数据
                if (rawValue instanceof String) {
                    JSONArray array = new JSONArray();
                    array.add(rawValue);
                    String value = parseMultiple(array);
                    if (StringUtils.isNotBlank(value)) {
                        return value;
                    }
                }
                log.error("无法解析的对象信息点.[信息点classCode={},code={}]", fieldMeta.getClassCode(), fieldMeta.getCode());
                return null;
            }
            return parseMultiple((JSONArray) rawValue);
        }
        if (fieldMeta.isRegion()) {
            if (!(rawValue instanceof JSONObject)) {
                log.error("无法解析的对象信息点.[信息点classCode={},code={}]", fieldMeta.getClassCode(), fieldMeta.getCode());
                return null;
            }
            return parseRegion((JSONObject) rawValue);
        }
        return parseShowText(rawValue);
    }


    private String parseMultiple(JSONArray array) {
        return array.stream().map(item -> {
            if (fieldMeta != null && fieldMeta.isRegion()) {
                JSONObject itemObj = (JSONObject) item;
                return this.parseRegion(itemObj);
            } else {
                return this.parseShowText(item);
            }
        }).collect(Collectors.joining("、"));
    }

    private String parseRegion(JSONObject obj) {
        Pair<Object, Object> region = FieldUtils.getRegion(obj);
        Object gt = region.getLeft();
        Object lt = region.getRight();
        return String.format("%s-%s", parseShowText(gt), parseShowText(lt));
    }

    private String parseShowText(Object obj) {
        if (obj == null) {
            return null;
        }
        if (fieldMeta == null) {
            return obj.toString();
        }
        //如果是附件,就什么都不显示
        if (getType() != DisplayMode.TEXT) {
            return null;
        }

        final String unit = fieldMeta.getUnit() != null ? fieldMeta.getUnit() : "";
        final FieldMeta.FieldMetaDataType dataType = fieldMeta.getDataType();
        //直接返回
        if (dataType == FieldMeta.FieldMetaDataType.INTEGER ||
                dataType == FieldMeta.FieldMetaDataType.DOUBLE ||
                dataType == FieldMeta.FieldMetaDataType.STRING ||
                dataType == FieldMeta.FieldMetaDataType.TEXT) {
            return obj + unit;
        }

        if (dataType == FieldMeta.FieldMetaDataType.DATETIME) {
            String formatter = fieldMeta.getInfoObject().getString("formater");
            return SafeDateUtil.tryGetDateTimeStr(String.valueOf(obj), formatter);
        }
        if (dataType == FieldMeta.FieldMetaDataType.DATE) {
            String formatter = fieldMeta.getInfoObject().getString("formater");
            return SafeDateUtil.tryGetDateStr(String.valueOf(obj), formatter);
        }
        if (dataType == FieldMeta.FieldMetaDataType.TIME) {
            String formatter = fieldMeta.getInfoObject().getString("formater");
            return SafeDateUtil.tryGetTimeStr(String.valueOf(obj), formatter);
        }


        //从datasource中取值
        if (dataType == FieldMeta.FieldMetaDataType.ENUM ||
                dataType == FieldMeta.FieldMetaDataType.BOOLEAN) {
            final Map<String, String> datasourceMap = fieldMeta.getDatasourceMap();
            String desc = datasourceMap.get(String.valueOf(obj));
            return StringUtils.isNotBlank(desc) ? desc : String.valueOf(obj);
        }

        return obj.toString();

    }


    @AllArgsConstructor
    public enum DisplayMode {
        TEXT(LocaleMessageUtil.getMessage("key_db82e835052e45ee893a465fea56d78f")),
        BLOB(LocaleMessageUtil.getMessage("key_d933d810be7b47f5afd00c4ee6fff6bf"));

        @Getter
        private final String desc;
    }
}
