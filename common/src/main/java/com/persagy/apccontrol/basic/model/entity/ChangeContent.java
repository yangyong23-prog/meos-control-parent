package com.persagy.apccontrol.basic.model.entity;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.persagy.apccontrol.basic.model.enumtype.SourceFromEnum;
import com.persagy.apccontrol.basic.structure.control.enumeration.ComputeTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author zhangyanchang
 * @version 1.0
 * @title: 下发指令的信息
 * @description: Never give up
 * @date: 2023/12/4 19:32
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ChangeContent {

    /**
     * 需要执行的时间
     */
    private String execTime;

    /**
     * 需要执行信息点编码
     */
    private String infoCode;

    /**
     * 需要执行的信息点值
     */
    private String infoValue;

    /**
     * 需要执行指令的对象ID
     */
    private String classCode;

    /**
     * 需要执行的指令来源
     */
    private SourceFromEnum sourceFrom;

    /**
     * 计算类型
     */
    private ComputeTypeEnum computeType;

    /**
     * 是否是自动 是 true ,false是人工 通过sourceFrom自动构建
     */
    private Boolean ai;
    /**
     * 批次号 用于批量执行
     */
    private String batchNumber;

    /**
     * 指令持续截止时间
     */
    private LocalDateTime timeDuration;

    /**
     * 干预截至时间
     */
    private String expireTime;

    @Deprecated
    public ChangeContent(String execTime, String infoCode, String infoValue, SourceFromEnum sourceFrom) {
        this.execTime = execTime;
        this.infoCode = infoCode;
        this.infoValue = infoValue;
        this.sourceFrom = sourceFrom;
        // 默认使用执行时间作为批次号 对历史逻辑进行兼容
        this.batchNumber = execTime;
        buildAiType(sourceFrom);
    }

    public ChangeContent(String execTime, String infoCode, String infoValue, SourceFromEnum sourceFrom, ComputeTypeEnum computeType) {
        this.execTime = execTime;
        this.infoCode = infoCode;
        this.infoValue = infoValue;
        this.sourceFrom = sourceFrom;
        // 默认使用执行时间作为批次号 对历史逻辑进行兼容
        this.batchNumber = execTime;
        this.computeType = computeType;
        buildAiType(sourceFrom);
    }

    @Deprecated
    public ChangeContent(String classCode, String execTime, String infoCode, String infoValue) {
        this.classCode = classCode;
        this.execTime = execTime;
        this.infoCode = infoCode;
        this.infoValue = infoValue;
        // 默认使用执行时间作为批次号 对历史逻辑进行兼容
        this.batchNumber = execTime;
    }

    public ChangeContent(String execTime, String infoCode, String infoValue) {
        this.execTime = execTime;
        this.infoCode = infoCode;
        this.infoValue = infoValue;
        // 默认使用执行时间作为批次号 对历史逻辑进行兼容
        this.batchNumber = execTime;
    }

    public ChangeContent(String execTime, String infoCode) {
        this.execTime = execTime;
        this.infoCode = infoCode;
        // 默认使用执行时间作为批次号 对历史逻辑进行兼容
        this.batchNumber = execTime;
    }

    /**
     * 直接用时间格式化后作为批次号
     *
     * @param time
     * @return
     */
    public static String buildBatchNumber(LocalDateTime time) {
        return String.valueOf(DateUtil.format(time, DatePattern.PURE_DATETIME_PATTERN));
    }

    /**
     * 转换执行时间
     *
     * @return 转换后的执行时间
     * @since 5.240402.0
     */
    public LocalDateTime toExecTime() {
        if (StringUtils.isBlank(execTime)) {
            return null;
        }
        return LocalDateTime.parse(execTime, DateTimeFormatter.ofPattern(DatePattern.PURE_DATETIME_PATTERN));
    }

    public void setSourceFrom(SourceFromEnum sourceFrom) {
        this.sourceFrom = sourceFrom;
        buildAiType(sourceFrom);
    }

    /**
     * 通过指令来源构建AI类型
     *
     * @param sourceFrom
     */
    public void buildAiType(SourceFromEnum sourceFrom) {
        if (sourceFrom == null) {
            return;
        }
        this.ai = sourceFrom == SourceFromEnum.AI_DEMAND;
    }
}
