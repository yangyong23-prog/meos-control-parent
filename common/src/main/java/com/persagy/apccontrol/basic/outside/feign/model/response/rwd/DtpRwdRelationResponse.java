package com.persagy.apccontrol.basic.outside.feign.model.response.rwd;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * 物理世界对象响应
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
@Data
@NoArgsConstructor(force = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DtpRwdRelationResponse extends BaseResponse {

    // ------------------------- 公共 -------------------------

    /**
     * 图类型编码
     */
    @NonNull
    private String relGraphCode;

    /**
     * 边类型编码
     */
    @NonNull
    private String relRelCode;

    // ------------------------- objFrom -------------------------

    /**
     * objFrom的主键
     */
    @NonNull
    private String fromId;

    /**
     * objFrom的对象名称
     */
    private String fromName;

    /**
     * objFrom的对象本地ID
     */
    private String fromLocalId;

    /**
     * objFrom的对象本地名称
     */
    private String fromLocalName;

    /**
     * objFrom的对象项目ID
     */
    private String fromProjectId;

    /**
     * objFrom的对象类型
     */
    private String fromObjType;

    /**
     * objFrom的类型编码
     */
    private String fromClassCode;

    /**
     * objFrom的valid
     */
    @Nullable
    private Boolean fromValid;

    /**
     * objFrom的infos
     */
    @Nullable
    private JSONObject fromInfos;

    // ------------------------- objTo -------------------------

    /**
     * objTo的主键
     */
    @NonNull
    private String toId;

    /**
     * objFrom的对象名称
     */
    private String toName;

    /**
     * objTo的对象本地ID
     */
    private String toLocalId;

    /**
     * objTo的对象本地名称
     */
    private String toLocalName;

    /**
     * objTo的对象项目ID
     */
    private String toProjectId;

    /**
     * objTo的对象类型
     */
    private String toObjType;

    /**
     * objTo的类型编码
     */
    private String toClassCode;

    /**
     * objTo的valid
     */
    @Nullable
    private Boolean toValid;

    /**
     * objTo的infos
     */
    @Nullable
    private JSONObject toInfos;

}
