package com.persagy.apccontrol.basic.outside.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author leifeng
 * @version 1.0.0
 * @ClassName AlarmResult
 * @Description 数据中台返回
 * @createTime 2021/9/23 12:02
 */
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DmpResult<T> implements Serializable {
    /**
     * 成功
     */
    public final static String SUCCESS = "success";
    /**
     * 失败
     */
    public final static String FAILURE = "fail";
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    @JsonAlias({"respCode", "code"})
    private String result;

    @JsonAlias({"respMsg", "msg"})
    private String message;

    @JsonAlias({"content"})
    private T data;

    private Integer count;

    /**
     * 数据总条数
     */
    private Integer total;

    public DmpResult() {
        super();
    }

    public DmpResult(String respCode, String respMsg) {
        this(respCode, respMsg, null, null, null);
    }

    public DmpResult(String respCode, String respMsg, T content) {
        this.result = respCode;
        this.message = respMsg;
        this.data = content;
    }


    public <T2> List<T2> getList(Class<T2> clazz) throws Exception {
        ArrayNode jsonNode = (ArrayNode) this.getData();
        if (jsonNode == null || jsonNode.isEmpty()) {
            return new ArrayList<T2>();
        }
        JavaType javaType = new ObjectMapper().getTypeFactory().constructParametricType(List.class, clazz);

        return new ObjectMapper().readValue(jsonNode.toString(), javaType);
    }


}
