package com.persagy.apccontrol.common.service.command.batch.dto;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;


import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.persagy.apccontrol.basic.model.enumtype.SourceFromEnum;
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
public class CommandBatchExecuteDTO {


    @Schema(description = "集团编码 如果为空自动设置为配置数据")
    private String groupCode;

    @Schema(description = "项目id 如果为空自动设置为配置数据")
    private String projectId;

    @Schema(description = "指令类型-时间表-手动干预-自动需求")
    private SourceFromEnum type;

    @Schema(description = "是否是自动 是 true")
    private Boolean ai;

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


    @Schema(description = "是否解除干预,来源页面操作")
    private Boolean releaseIntervention;

    @Schema(description = "指令持续截止时间")
    private LocalDateTime timeDuration;

    @Schema(description = "扩充字段")
    private Object extend;

    /**
     * 执行批次号
     */
    private String batchNumber;

    /**
     * 下发之前的孪生IOT值
     */
    private String beforeValue;

    /** --------- 执行结果数据 ------------*/
    /**
     * 执行状态 默认成功
     * 默认 -1 没有执行
     * 0：执行失败
     * 1：执行成功
     */
    private Integer execStatus = 1;
    /**
     * 下发的指令id
     */
    private Integer equipRecordId;
    /**
     * 执行失败信息
     */
    private String errorMessage;


    public CommandBatchExecuteDTO() {
    }

    /**
     * 对数据进行 分组-按照设备信息点分组
     *
     * @param dto
     */
    public static Map<String, List<CommandBatchExecuteDTO>> getGroupDTOByInfoCode(List<CommandBatchExecuteDTO> dto) {
        Map<String, List<CommandBatchExecuteDTO>> map = new HashMap<>();

        if (CollectionUtils.isEmpty(dto)) {
            return map;
        }
        dto.forEach(i -> {
            String key = i.getInfoCode();
            if (map.containsKey(key)) {
                map.get(key).add(i);
            } else {
                List<CommandBatchExecuteDTO> list = new ArrayList<>();
                list.add(i);
                map.put(key, list);
            }
        });
        return map;
    }

    public void check() {

        boolean o = StringUtils.isBlank(objectId);
        boolean i = StringUtils.isBlank(infoCode);
        boolean v = this.value == null;
        boolean t = this.type == null;

        if (o || i || v || t) {
            this.execStatus = 0;
            this.errorMessage = this.errorMessage + LocaleMessageUtil.getMessage("key_d7b45b26886145358f47c98f56c41ced") + this;
        }
    }

    public void setErrorMessage(String msg) {
        this.execStatus = 0;
        this.errorMessage = this.errorMessage + msg;
    }

    @Data
    public static class TimeHorizon {

        @Schema(description = "开始执行时间")
        private LocalDateTime startTime;

        @Schema(description = "持续时长")
        private Integer sustainTime = 0;

    }

    /**
     * 为空返回 人工
     *
     * @return
     */
    public Boolean getAi() {
        if (ai == null) {
            return false;
        }
        return ai;
    }

    public Boolean getReleaseIntervention() {
        if (releaseIntervention == null) {
            return false;
        }
        return releaseIntervention;
    }
}
