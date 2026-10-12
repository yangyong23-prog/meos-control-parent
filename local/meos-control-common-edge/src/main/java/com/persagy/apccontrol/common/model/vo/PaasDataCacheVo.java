package com.persagy.apccontrol.common.model.vo;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

/**
 * 边到云数据同步，走kafka，实体类
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/7/10 17:19
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PaasDataCacheVo {

    /**
     * 每次限制100条
     */
    public static final int MAX_LIMIT = 100;

    /**
     * 数据的key。格式：,{"id":1793921843626475614,"paas_table":"rwd_instance_measure_node_relation","paas_schema":"apm-exp"} paas_schema是库名，id是主键，paas_table是表名
     */
    private String key;

    /**
     * 数据的value。格式：,{,数据库字段:数据库字段的值,}。value部分如果为空，则表示该条数据需要删除。示例：
     * {"id":"ffeee44cee074773ada21d0fe917656b",,"group_code":"WNMDGFZQNY000",,"project_id":"Pj3205830005",,
     * "object_id":"Eqys45694380556371f76465c14158a06e82421c97ce9e",,"class_code":"ACGEHE11",,"info_code":"lowValveOpening",
     * ,"protocol_type":"UDP",,"brand":"persagy",,"key_info":"{\"key\":\"点位key\",\"gwId\":\"gwId\"}",
     * ,"value_info":"{\"key\":\"1101080259-20256-101\",\"gwId\":\"20256-101\"}",,"data_type":null,,"is_finaly":0,
     * ,"creator":"0",,"creation_time":"20230411192647",,"modifier":"0",,"modified_time":"20230411192647",,"valid":1,
     * ,"ts":"2023-11-2014:57:29",,"table_name":"dt_iot_server_point",}
     */
    private String value;

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaasDataCacheKey {

        // 主键ID
        @JsonProperty(index = 0)
        private String id;

        // 表名
        @JsonProperty(index = 1)
        private String paas_table;

        // 库名
        @JsonProperty(index = 2)
        private String paas_schema;

        // json列
        @JsonProperty(index = 3)
        private String json_columns;

        @SneakyThrows
        public String toJsonString() {
            return SpringUtil.getBean(ObjectMapper.class).writeValueAsString(this);
        }
    }
}
