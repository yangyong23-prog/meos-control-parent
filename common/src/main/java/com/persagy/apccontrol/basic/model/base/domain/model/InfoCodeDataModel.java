package com.persagy.apccontrol.basic.model.base.domain.model;


import com.persagy.apccontrol.basic.model.base.domain.ValueObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 信息点值模型
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor
public class InfoCodeDataModel implements ValueObject {

    /**
     * 信息点编码
     */
    @NonNull
    private String infoCode;

    /**
     * 信息点值
     */
    @Nullable
    private Object dataValue;

    /**
     * 数据时间
     */
    @Nullable
    private LocalDateTime dataTime;

    /**
     * 拓展值
     */
    @Nullable
    private Map<String, Object> additionalMapValue;

}
