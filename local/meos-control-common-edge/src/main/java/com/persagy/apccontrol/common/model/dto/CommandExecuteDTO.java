package com.persagy.apccontrol.common.model.dto;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;


import com.alibaba.nacos.common.utils.CollectionUtils;
import com.persagy.apccontrol.basic.model.enumtype.SourceFromEnum;
import com.persagy.apccontrol.basic.utils.ParameterCheck;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 指令下发对象
 *
 * @author yuyao
 */
@Data
@Accessors(chain = true)
public class CommandExecuteDTO {


    @Schema(description = "集团编码 如果为空自动设置为配置数据")
    private String groupCode;

    @Schema(description = "项目id 如果为空自动设置为配置数据")
    private String projectId;

    @Schema(description = "指令类型-时间表-手动干预-自动需求")
    private SourceFromEnum type;

    @Schema(description = "系统ID")
    private String systemId;
    @Schema(description = "系统编码")
    private String systemCode;

    @Schema(description = "设备id")
    private String objectId;

    @Schema(description = "信息点  冷机台数信息点特殊处理为： chillerNum")
    private String infoCode;

    @Schema(description = "信息点设定值 冷机台数 值特殊处理 1,2 小机1台大机2台")
    private Object value;

    @Schema(description = "指令执行时间 如果为空自动设置为当前时间")
    private LocalDateTime time;


    @Schema(description = "是否是人工干预指令 true 是 fase 解除干预")
    private Boolean manualIntervention;

    @Schema(description = "指令持续截止时间")
    private LocalDateTime timeDuration;

    @Schema(description = "扩充字段")
    private Object extend;

    /**
     * 下发之前的孪生IOT值
     */
    private String beforeValue;

    /**
     * 执行状态 默认成功
     * 默认 -1 没有执行
     * 0：执行失败
     * 1：执行成功
     */
    private Integer execStatus;

    public CommandExecuteDTO(String groupCode, String projectId, String objectId) {
        this.groupCode = groupCode;
        this.projectId = projectId;
        this.objectId = objectId;
    }


    public CommandExecuteDTO(String objectId) {
        this.objectId = objectId;
    }


    public CommandExecuteDTO() {
    }

    public CommandExecuteDTO(String objectId, String infoCode, String value, LocalDateTime timeDuration) {
        this.objectId = objectId;
        this.infoCode = infoCode;
        this.value = value;
        this.timeDuration = timeDuration;
    }

    public void check() {

        ParameterCheck.isNull(objectId, LocaleMessageUtil.getMessage("key_dd862e3de4114aa9955f037b82cb03f9"));
        ParameterCheck.isNull(infoCode, LocaleMessageUtil.getMessage("key_746211739a8144279ca09edf2eef0ceb"));
        ParameterCheck.isNull(value, LocaleMessageUtil.getMessage("key_deaa8a80b2704b6fb9566493e310ad77"));
        ParameterCheck.isNull(type, LocaleMessageUtil.getMessage("key_b27a7e71abd640ae80872d8ec1c320eb"));
    }

    @Data
    public static class TimeHorizon {

        @Schema(description = "开始执行时间")
        private LocalDateTime startTime;

        @Schema(description = "持续时长")
        private Integer sustainTime = 0;

    }


    /**
     * 对数据进行 分组-按照设备信息点分组
     *
     * @param dto
     */
    public static Map<String, List<String>> getGroupDTOByInfoCode(List<CommandExecuteDTO> dto) {
        Map<String, List<String>> map = new HashMap<>();

        if (CollectionUtils.isEmpty(dto)) {
            return map;
        }
        dto.forEach(i -> {
            String key = i.getInfoCode();
            if (map.containsKey(key)) {
                map.get(key).add(i.getObjectId());
            } else {
                List<String> list = new ArrayList<>();
                list.add(i.getObjectId());
                map.put(key, list);
            }
        });
        return map;
    }
}
