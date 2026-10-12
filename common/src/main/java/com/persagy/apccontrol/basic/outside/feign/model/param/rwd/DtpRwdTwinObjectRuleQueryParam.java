package com.persagy.apccontrol.basic.outside.feign.model.param.rwd;

import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 孪生对象规则查询参数
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
@Data
public class DtpRwdTwinObjectRuleQueryParam {

    /**
     * 对象实例ID
     */
    @NonNull
    @NotNull(message = "{key_8ed7086a90aa4ce3be81d08bacfa7340}")
    private String classCode;

    /**
     * 信息点编码
     */
    private Set<String> infoCodes;

    /**
     * 构造器
     *
     * @param classCode 设备类编码
     * @since 5.240701.0
     */
    public DtpRwdTwinObjectRuleQueryParam(@NonNull DtpRwdClassCodeEnum classCode) {
        this(classCode, null);
    }

    /**
     * 构造器
     *
     * @param classCode 设备类编码
     * @param infoCodes 信息点编码
     * @since 7.250102.0
     */
    public DtpRwdTwinObjectRuleQueryParam(@NonNull DtpRwdClassCodeEnum classCode, @Nullable Set<String> infoCodes) {
        this(classCode.value(), infoCodes);
    }

    /**
     * 构造器
     *
     * @param classCode 设备类编码
     * @since 5.240701.0
     */
    public DtpRwdTwinObjectRuleQueryParam(@NonNull String classCode) {
        this(classCode, null);
    }

    /**
     * 构造器
     *
     * @param classCode 设备类编码
     * @param infoCodes 信息点编码
     * @since 7.250102.0
     */
    public DtpRwdTwinObjectRuleQueryParam(@NonNull String classCode, @Nullable Collection<String> infoCodes) {
        this.classCode = classCode;
        this.infoCodes = CollectionUtils.isNotEmpty(infoCodes) ? new HashSet<>(infoCodes) : null;
    }
}
