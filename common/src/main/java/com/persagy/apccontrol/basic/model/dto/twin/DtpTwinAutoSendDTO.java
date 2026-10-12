package com.persagy.apccontrol.basic.model.dto.twin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 自控参数下发
 *
 * @author yuyao
 */
@Data
public class DtpTwinAutoSendDTO {

    public static final int CLEAR_DATA = -9998;

    @Schema(description = "设备id")
    private String objectId;

    @Schema(description = "控制参数-参数编码和参数值的值对-{\"chillerSNumber\": 1,\"chillerLNumber\":2}")
    private Map<String, Object> data;


    public DtpTwinAutoSendDTO(String objectId, String infoCode, String value) {
        this.objectId = objectId;
        setParameter(infoCode, value);
    }

    public DtpTwinAutoSendDTO() {
    }

    /**
     * 指令是否全部为清空指令
     *
     * @return 指令是否全部为清空指令
     * @since 7.250801.0
     */
    @JsonIgnore
    public boolean allClear() {
        if (MapUtils.isEmpty(data)) {
            return false;
        }
        return data.values().stream().allMatch(item -> Objects.nonNull(item) && ((
                item instanceof Integer && CLEAR_DATA == (int) item)
                || (item instanceof BigDecimal && CLEAR_DATA == ((BigDecimal) item).intValue())
                || (item instanceof String && StringUtils.equals(String.valueOf(CLEAR_DATA), (String) item))));
    }

    /**
     * 设定执行数据
     *
     * @param infoCode
     * @param value
     */
    public void setParameter(String infoCode, String value) {
        if (data == null) {
            data = new HashMap<>();
        }
        data.put(infoCode, value);
    }

    public void setParameter(Map<String, Object> zl) {
        if (zl == null) {
            return;
        }
        data.putAll(zl);
    }

}

