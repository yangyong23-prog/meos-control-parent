package com.persagy.apccontrol.common.model.dto;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import lombok.*;

import java.util.List;
import java.util.Set;

/**
 * 类功能描述
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/2/19 15:11
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ExecSend {

    private boolean auto;
    private int failureNum;
    private StringBuilder stringBuilder;

    /***   20240823薛涛新增 谨慎使用*/
    /** 总条数*/
    private Integer allCount;

    /** 失败对象数据*/
    private List<DtpRwdObjectDTO> errorCountObject;

    /** 不可控对象数据*/
    private Set<DtpRwdObjectDTO> noControlObject;

    /** 成功条数*/
    private Integer successCount;

    /**构造*/
    public ExecSend(boolean auto, int failureNum, StringBuilder stringBuilder) {
        this.auto = auto;
        this.failureNum = failureNum;
        this.stringBuilder = stringBuilder;
    }

}
