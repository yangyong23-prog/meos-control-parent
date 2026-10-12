package com.persagy.apccontrol.basic.model.vo.systemgraph;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;

/**
 * 字段元数据
 *
 * @author chenlong
 * @date 2022/1/19 4:01 下午
 */
@Data
@Accessors(chain = true)
@ApiModel("字段元数据")
public class FieldMeta {
    @ApiModelProperty("信息点")
    private String name;
    @ApiModelProperty("信息点编码")
    private String code;
    @ApiModelProperty("类编码")
    private String classCode;
    @ApiModelProperty("数据类型")
    private FieldMetaDataType dataType;
    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("是否是多个")
    private boolean multiple;
    @ApiModelProperty("是否是范围")
    private boolean region;

    private transient LazyField<List<DataSourceVo>> dataSource = LazyField.by(() -> {
        String ds = getInfoObject().getString("dataSource");
        List<DataSourceVo> poiDataSourceDtos = JSON.parseArray(ds, DataSourceVo.class);
        return poiDataSourceDtos != null ? poiDataSourceDtos : new ArrayList<>(0);
    });
    private transient LazyField<Map<String, String>> dataSourceMap = LazyField.by(() -> {
        return dataSource.get().stream().collect(toMap(DataSourceVo::getCode, DataSourceVo::getName));
    });

    public static <T, K, U> Collector<T, ?, Map<K, U>> toMap(Function<? super T, ? extends K> keyMapper,
                                                             Function<? super T, ? extends U> valueMapper) {
        return Collector.of(
                HashMap::new,
                (result, item) -> {
                    result.put(keyMapper.apply(item), valueMapper.apply(item));
                },
                (result1, result2) -> {
                    result1.putAll(result2);
                    return result1;
                });
    }

    private transient JSONObject infoObject = new JSONObject();

    @JsonIgnore
    @JSONField(serialize = false, deserialize = false)
    public Map<String, String> getDatasourceMap() {
        return dataSourceMap.get();
    }

    @ApiModelProperty("数据源")
    @JSONField(name = "dataSource")
    public @Nullable List<DataSourceVo> getDataSource() {
        return dataSource.get();
    }

    public enum FieldMetaDataType {

        STRING(ObjectField.DisplayMode.TEXT),
        TEXT(ObjectField.DisplayMode.TEXT),
        BOOLEAN(ObjectField.DisplayMode.TEXT),
        ENUM(ObjectField.DisplayMode.TEXT),
        DATETIME(ObjectField.DisplayMode.TEXT),
        DATE(ObjectField.DisplayMode.TEXT),
        TIME(ObjectField.DisplayMode.TEXT),
        INTEGER(ObjectField.DisplayMode.TEXT),
        DOUBLE(ObjectField.DisplayMode.TEXT),
        ATTACHMENT(ObjectField.DisplayMode.BLOB),
        PICTURE(ObjectField.DisplayMode.BLOB),
        POSITION_MAP(ObjectField.DisplayMode.BLOB),
        SIMPLE_OBJECT(ObjectField.DisplayMode.BLOB);
        @Getter
        private final ObjectField.DisplayMode displayMode;

        FieldMetaDataType(ObjectField.DisplayMode displayMode) {
            this.displayMode = displayMode;
        }

        public static FieldMetaDataType getByCode(String code) {
            for (FieldMetaDataType type : values()) {
                if (StringUtils.equalsIgnoreCase(code, type.name())) {
                    return type;
                }
            }
            if (StringUtils.equalsIgnoreCase(code, "PIC")) {
                return PICTURE;
            }
            if (StringUtils.equalsIgnoreCase(code, "MENUM")) {
                return ENUM;
            }
            return null;
        }
    }

}
