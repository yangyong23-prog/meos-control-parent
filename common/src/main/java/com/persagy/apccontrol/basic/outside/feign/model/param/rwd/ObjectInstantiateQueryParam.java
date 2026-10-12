package com.persagy.apccontrol.basic.outside.feign.model.param.rwd;

import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.common.DtpRwdCommonParam;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.lang.NonNull;

import javax.validation.constraints.NotNull;

/**
 * 物理世界对象规则实例化查询参数
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ObjectInstantiateQueryParam extends DtpRwdCommonParam {

    /**
     * 接口限制每次最大查询100条
     */
    public static final int MAX_QUERY_SIZE = 1000;

    /**
     * 构造器
     *
     * @param groupCode 集团编码
     * @param projectId 项目ID
     * @since v5.5.0
     */
    protected ObjectInstantiateQueryParam(String groupCode, String projectId) {
        super(groupCode, projectId);
    }

    /**
     * 对象实例ID
     */
    @NotNull(message = "{key_849e6bb6ea174cd5b11b5bf4a5d4092e}")
    private String objectId;

    /**
     * 信息点编码
     */
    @NotNull(message = "{key_0ad0f2789ec14197b646fbf159c47354}")
    private String infoCode;

    /**
     * 创建当前实例
     *
     * @param groupCode 集团编码
     * @param projectId 项目ID
     * @return 当前实例
     * @since v5.5.0
     */
    public static ObjectInstantiateQueryParam instance(@NonNull String groupCode, @NonNull String projectId) {
        return new ObjectInstantiateQueryParam(groupCode, projectId);
    }

    /**
     * 创建当前实例
     *
     * <p>仅支持批量查询使用
     *
     * @param objectId 对象实例ID
     * @param infoCode 信息点编码
     * @return 当前实例
     * @since v6.0.0
     */
    public static ObjectInstantiateQueryParam batchInstance(@NonNull String objectId, @NonNull String infoCode) {
        return new ObjectInstantiateQueryParam(null, null)
                .setObjectId(objectId).setInfoCode(infoCode);
    }
}
