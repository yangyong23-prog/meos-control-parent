package com.persagy.apccontrol.common.logtrace.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class BasePo {

    @TableId(
            type = IdType.ASSIGN_ID
    )
    private Long id;

    private LocalDateTime createTime;

    private LocalDateTime lastUpdateTime;

    private String application;
}
