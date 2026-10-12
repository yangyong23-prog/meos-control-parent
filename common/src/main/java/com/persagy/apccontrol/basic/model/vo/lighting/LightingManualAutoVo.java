package com.persagy.apccontrol.basic.model.vo.lighting;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 照明手自动数量和设备集合
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2025/3/27 14:06
 * @since 7.250202.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LightingManualAutoVo {

    /**
     * 自动数量
     */
    private Integer number;

    /**
     * 实例id集合
     */
    private List<String> instanceIds;

}
