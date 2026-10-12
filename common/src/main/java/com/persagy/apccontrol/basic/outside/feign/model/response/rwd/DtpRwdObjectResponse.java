package com.persagy.apccontrol.basic.outside.feign.model.response.rwd;

import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

import java.util.Set;

/**
 * 物理世界对象响应
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DtpRwdObjectResponse extends BaseResponse {

    /**
     * 对象ID
     */
    private String id;

    /**
     * 对象名称
     */
    private String name;

    /**
     * 对象本地ID
     */
    private String localId;

    /**
     * 对象本地名称
     */
    private String localName;

    /**
     * 项目ID
     */
    private String projectId;

    /**
     * 对象类型
     */
    private String objType;

    /**
     * 对象编码
     */
    private String classCode;

    /**
     * 集团编码
     */
    private String groupCode;

    /**
     * 是否有效
     */
    private Boolean valid;

    /**
     * 关联对象ID
     */
    @Nullable
    private Set<String> relObjIds;

    /**
     * 获取动态名称
     *
     * @return 动态名称
     * @since 24Q1 T3S2-5.18.0
     */
    public String getDynamicName() {
        return StringUtils.defaultString(localName, name);
    }
}
