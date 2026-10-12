package com.persagy.apccontrol.basic.model.vo.rules;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @ClassName 薛涛
 * @Description 下发指令统一返回
 * @Author Jiangnan Cui
 * @Date 2024/8/22 16:42
 * @Version 1.0
 */
@Data
public class SetOperatingParametersVO {


    /** 成功条数*/
    private Integer successCount = 0;

    /** 失败条数*/
    private Integer errorCount  = 0;

    /** 不可控条数*/
    private Integer noControlCount  = 0;

    /** 总条数*/
    private Integer allCount  = 0;

    /** 失败对象数据*/
    private List<DtpRwdObjectDTO>  errorCountObject;

    /** 不可控对象数据*/
    private Set<DtpRwdObjectDTO> noControlObject;

    /** 是否全部成功*/
    private Boolean isAllSuccess;

    /** 老得失败数据*/
    private Map<String, String> objectIdAndErrorMessages;


}
