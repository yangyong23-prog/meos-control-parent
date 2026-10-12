package com.persagy.apccontrol.basic.outside.feign.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * @author shaohongbo
 * @date 2023年03月29日 20:04
 */
@Data
public class DtpTwinR<T> implements Serializable {

    protected static final String SUCCESS = "success";
    protected static final String FAIL = "fail";
    @JsonInclude(Include.NON_NULL)
    @JsonAlias({"respCode", "code"})
    private String respCode;

    @JsonInclude(Include.NON_NULL)
    @JsonAlias({"respMsg", "message"})
    private String respMsg;
    @JsonInclude(Include.NON_NULL)
    @JsonAlias({"cause"})
    private String cause;

    @JsonInclude(Include.NON_NULL)
    @JsonAlias({"content", "data"})
    private List<T> content;

    @JsonInclude(Include.NON_NULL)
    private Integer count;

    @JsonIgnore
    public boolean getIsSuccess() {
        return isSuccess(this.respCode);
    }

    public static boolean isSuccess(String code) {
        return Objects.equals(code, "00000");
    }

}
