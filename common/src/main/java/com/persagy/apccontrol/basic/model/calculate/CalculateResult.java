package com.persagy.apccontrol.basic.model.calculate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Field;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 价值计算--综合结果
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/9/7 18:43
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CalculateResult {

    // ------------------------- 价值计算对结果 -------------------

    @Schema(description = "实际开关机时间点")
    private LocalTime workTime;

    @Schema(description = "当前价值计算结果--温度数据")
    private TreeMap<LocalTime, CalculateResultDetail> tdb;

    @Schema(description = "当前价值计算结果--温度数据")
    private TreeMap<LocalTime, CalculateResultDetail> cO2;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>(16);
        try {
            Field[] fields = this.getClass().getDeclaredFields();
            for (Field field : fields) {
                try {
                    map.put(field.getName(), field.get(this));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return map;
    }

}
