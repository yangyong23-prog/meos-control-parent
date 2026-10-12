package com.persagy.apccontrol.basic.model.message.mq;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 关系图消息
 *
 * @author MAF
 * @date 2024/07/04
 */
@Data
public class DtpRelationalGraphMessage {

    private String messageType;
    private String dicVersion;
    private List<ClassCodeMessage> classCodes;

    /**
     * 设备类
     *
     * @author MAF
     * @date 2024/07/04
     */
    @Data
    public static class ClassCodeMessage {
        private String classCode;
        private List<GraphTypeMessage> graphTypes;
    }

    /**
     * 图形类型
     *
     * @author MAF
     * @date 2024/07/04
     */
    @Data
    public static class GraphTypeMessage {

        private String graphTypeCode;
        private List<TemplateMessage> templates;
        private int count_insert;
        private int count_update;
        private int count_delete;
    }

    /**
     * 模板
     *
     * @author MAF
     * @date 2024/07/04
     */
    @Data
    public static class TemplateMessage {

        private String id;
        private String oriId;
        private Integer version;
        private LocalDateTime modifiedTime;
        private String change;
    }

}
