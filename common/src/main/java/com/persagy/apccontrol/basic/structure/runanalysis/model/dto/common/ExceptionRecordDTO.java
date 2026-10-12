package com.persagy.apccontrol.basic.structure.runanalysis.model.dto.common;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

/**
 * 异常记录DTO
 *
 * @author JiYinchuan
 * @since 7.260201.0
 */
@Data
public class ExceptionRecordDTO {

    /**
     * 触发时间
     */
    private LocalDateTime triggerTime;

    /**
     * 恢复时间
     */
    private @Nullable LocalDateTime recoverTime;

    /**
     * 对象
     */
    private DtpRwdObjectDTO object;

    /**
     * 信息点
     */
    private String infoCode;

    /**
     * 信息点值
     */
    private String infoCodeValue;

}
