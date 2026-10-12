package com.persagy.apccontrol.basic.model.vo.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 系统图数据接口
 * @author tangzy
 * @date 2023年03月17日 15:50
 */
@Data
public class RcEqInfoCodeCfgVO {
    /**
     * 配置ID
     */
    @Schema(description ="配置ID")
    private Integer id;
    /**
     * 项目id
     */
    @Schema(description ="项目id")
    private String projectId;
    /**
     * 设备id
     */
    @Schema(description ="设备id")
    private String equipmentId;
    /**
     * 设备类
     */
    @Schema(description ="设备类")
    private String classCode;
    /**
     * 信息点
     */
    @Schema(description ="信息点")
    private String infoCode;
    /**
     * 信息点名称
     */
    @Schema(description ="信息点名称")
    private String name;
    /**
     * 信息点别名
     */
    @Schema(description ="信息点别名")
    private String alias;
    /**
     * 单位名称
     */
    @Schema(description ="单位名称")
    private String unit;
    /**
     * 换算系数。页面展示的数值=采集数据*换算系数
     */
    @Schema(description ="换算系数。页面展示的数值=采集数据*换算系数")
    private Double scale;
    /**
     * 展示位置，枚举类型。如页面展示的数值=采集数据*换算系数
     */
    @Schema(description ="展示位置，枚举类型。如页面展示的数值=采集数据*换算系数")
    private String displayPosition;
    /**
     * 最大值
     */
    @Schema(description ="最大值")
    private Integer max;
    /**
     * 最小值
     */
    @Schema(description ="最小值")
    private Integer min;
    /**
     * 展示顺序
     */
    @Schema(description ="展示顺序")
    private Integer ordered;
    /**
     * 参数类型：运行参数，设定参数
     */
    @Schema(description ="参数类型：运行参数，设定参数")
    private String parameterType;
    /**
     * 扩展信息
     */
    @Schema(description ="扩展信息")
    private String extendInfo;
    /**
     * 创建时间
     */
    @Schema(description ="创建时间")
    private Date createTime;
    /**
     * 同步时间
     */
    @Schema(description ="同步时间")
    private Date sysTime;
    /**
     * 系统Id
     */
    @Schema(description ="系统ID")
    private String systemId;

}
