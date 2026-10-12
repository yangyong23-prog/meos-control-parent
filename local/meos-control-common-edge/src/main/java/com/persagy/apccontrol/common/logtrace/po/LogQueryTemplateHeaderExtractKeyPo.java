package com.persagy.apccontrol.common.logtrace.po;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("logtrace_querytemplate_header_extractkey_relation")
public class LogQueryTemplateHeaderExtractKeyPo extends BasePo {


    private  Long headerId;
    /**
     * 表头映射所属业务对象code
     */
    private String extractObject;

    /**
     * 表头映射所属业务对象中对应的key
     */
    private String extractKey;




}
