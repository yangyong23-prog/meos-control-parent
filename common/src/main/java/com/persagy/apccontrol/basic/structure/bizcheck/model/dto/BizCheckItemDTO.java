package com.persagy.apccontrol.basic.structure.bizcheck.model.dto;

import com.persagy.apccontrol.basic.structure.bizcheck.enumeration.BizCheckStatusEnum;
import com.persagy.apccontrol.basic.structure.bizcheck.enumeration.BizCheckItemTagEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 *检查项结果详情DTO
 *
 * @author wxf
 * @date 2026-02-27
 */
@Data
public class BizCheckItemDTO<T> {

    /**
     *检查结果状态
     * SUCCESS:成功, FAILED:失败, WARNING:警告
     */
    protected BizCheckStatusEnum status;

    /**
     * 检查内容
     */
    private T checkResult;
}