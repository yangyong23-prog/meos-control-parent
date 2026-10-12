package com.persagy.apccontrol.basic.model.vo.system;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.persagy.apccontrol.basic.model.enumtype.system.DataTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.system.LogTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.system.OperateTypeEnum;
import com.persagy.apccontrol.basic.model.order.ConfigResultMsgOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 系统日志表
 *
 * @author tzy
 * @TableName rc_sys_log
 */
@Data
@Slf4j
@JsonInclude(value = JsonInclude.Include.ALWAYS)
public class SystemOperationRecordsVO {
    /**
     * 日志id
     */
    @Schema(description = "日志id")
    private String id;
    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private String createUser;
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /**
     * 业务类型
     */
    @Schema(description = "业务类型")
    private String type;
    /**
     * 操作类型
     */
    @Schema(description = "操作类型")
    private String operateType;

    /**
     * add 新增 delete删除
     */
    @Schema(description = "数据类型")
    private String dataType;
    /**
     * 操作对象名称
     */
    @Schema(description = "操作对象名称")
    private String objectName;

    @Schema(description = "对象类型")
    private String classCodeName;
    /**
     * 操作内容
     */
    @Schema(description = "操作内容")
    private String content;
    /**
     * 操作内容
     */
    @Schema(description = "状态")
    private String memo;
    /**
     * 修改前内容
     */
    @Schema(description = "修改前内容")
    private String beforeModify;
    /**
     * 接口ip地址
     */
    @Schema(description = "接口ip地址")
    private String ip;
    /**
     * 日志结果信息
     */
    @Schema(description = "日志结果信息")
    private String resultMsg;

    /**
     * 规则ID
     */
    @Schema(description = "规则ID")
    private String configId;

    /**
     * 状态
     */
    @Schema(description = "状态")
    private String status;

    public static void getPage(Page<SystemOperationRecordsVO> pageSystemOperationRecords) {
        try {
            for (SystemOperationRecordsVO record : pageSystemOperationRecords.getRecords()) {
                String resultMsg = record.getResultMsg();
                if (JSONUtil.isTypeJSON(resultMsg)) {
                    ConfigResultMsgOrder configResultMsgOrder = JSONObject.parseObject(resultMsg, ConfigResultMsgOrder.class);
                    BeanUtil.copyProperties(configResultMsgOrder, record, CopyOptions.create().ignoreNullValue());
                } else {
                    record.setContent(resultMsg);
                }
                LogTypeEnum logType = LogTypeEnum.getEnumByCode(record.getType());
                record.setType(ObjectUtil.isNotNull(logType) ? logType.getDesc() : null);
                OperateTypeEnum operateType = OperateTypeEnum.getEnumByCode(record.getOperateType());
                record.setOperateType(ObjectUtil.isNotNull(operateType) ? operateType.getDesc() : null);
                DataTypeEnum dataType = DataTypeEnum.getEnumByCode(record.getDataType());
                record.setDataType(ObjectUtil.isNotNull(dataType) ? dataType.getDesc() : null);
            }
        } catch (Exception e) {
            log.error("查询失败", e);
        }
    }
}
