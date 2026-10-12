package com.persagy.apccontrol.basic.resource.param;

import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Collection;

/**
 * 基础配置表查询参数
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
@Data
public class RcBasicsConfigQueryDaoParam {

    /**
     * 项目ID
     */
    private String projectId;

    /**
     * 配置类型
     */
    private BasicsConfigTypeEnum type;

    /**
     * 子配置类型
     */
    private String childType;

    /**
     * 子配置类型
     */
    private Collection<String> childTypes;

    /**
     * 配置状态(有效无效)
     */
    private StateTypeEnum st = StateTypeEnum.VALID;

    /**
     * 建筑编码
     */
    private String architectureCode;

    /**
     * 建筑编码
     */
    private Collection<String> architectureCodes;

    /**
     * 楼层编码
     */
    private String floorCode;

    /**
     * 楼层编码
     */
    private Collection<String> floorCodes;

    /**
     * 房间编码
     */
    private String roomCode;

    /**
     * 房间编码
     */
    private Collection<String> roomCodes;

    /**
     * 设备编码
     */
    private String facilityCode;

    /**
     * 设备编码
     */
    private Collection<String> facilityCodes;

    /**
     * 系统编号
     */
    private String systemId;

    /**
     * 系统编号
     */
    private Collection<String> systemIds;

    /**
     * 表达式
     */
    private String spEl;

    /**
     * 表达式
     */
    private Collection<String> spEls;

    /**
     * 配置生效时间
     */
    private LocalDateTime startTime;

    /**
     * 配置失效时间
     */
    private LocalDateTime endTime;

}
