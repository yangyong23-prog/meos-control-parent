package com.persagy.apccontrol.basic.model.dto;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collection;

/**
 * 能耗数据详情查询参数
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
@Data
@ToString(callSuper = true)
public class EnergyDataInfoQueryParam {

    /**
     * 对象实例ID
     *
     * <p>分项为 [rwdId], 负载为 [id]
     */
    @NotBlank(message = "{key_849e6bb6ea174cd5b11b5bf4a5d4092e}")
    private String objectId;

    /**
     * 仪表的本地编码
     *
     * 关联数据的使用使用
     */
    private String meterId;


    /**
     * 信息点编码列表
     */
    @Size(min = 1, message = "{key_8b4df484c4e64d24879c00c12d16cd67}")
    private Collection<String> infoCodes = new ArrayList<>();

    /**
     * 查询时间点(为空时取最新)
     */
    private String ipTime;

    /**
     * 开始时间
     */
    private String startTime;

}
