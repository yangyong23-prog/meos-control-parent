package com.persagy.apccontrol.basic.model.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author ：wzq
 * @description：
 * @date ：Created in 2023/10/10 2:50 PM
 */
@Data
public class CurveVO {

    private String objectId;
    private String objectName;
    private List<CurveInfoCodeVO> data;



    @Data
    public static class  CurveInfoCodeVO{

        private String infoCode;
        private String infoCodeName;
        private String objectId;
        private String objectName;
        private List<Map<String, Object>> data;
    }

}
