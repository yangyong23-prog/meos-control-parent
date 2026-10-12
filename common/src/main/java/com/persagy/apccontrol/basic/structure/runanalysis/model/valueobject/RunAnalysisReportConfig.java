package com.persagy.apccontrol.basic.structure.runanalysis.model.valueobject;

import com.persagy.apccontrol.basic.model.base.domain.ValueObject;
import lombok.Data;

@Data
public class RunAnalysisReportConfig implements ValueObject {
    //章节id
    protected Long id;
    //章节名称
   protected String name;
    //章节排序
    protected int sort;
   //章节是否选中
   protected String checked;
   //章节对象ID
   protected String objectId;
   //父章节
   protected String parent;
}