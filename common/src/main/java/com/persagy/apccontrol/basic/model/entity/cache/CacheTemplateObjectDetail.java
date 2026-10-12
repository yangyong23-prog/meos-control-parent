package com.persagy.apccontrol.basic.model.entity.cache;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;

/**
 * 缓存模板对象信息点明细实体。
 *
 * <p>每条记录保存一个模板、根对象和信息点对应的完整有序结果列表，关联对象层级
 * 以内嵌 JSON 形式自包含存储。</p>
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@FieldNameConstants
@TableName(value = "cache_template_object_detail")
public class CacheTemplateObjectDetail extends BaseAssignIdDO implements Serializable {

    /**
     * 序列化版本号。
     *
     * @since v7.260701.0
     */
    private static final long serialVersionUID = -3741978655748477632L;

    /**
     * 项目 ID。
     *
     * @since v7.260701.0
     */
    @TableField(value = "project_id")
    private String projectId;

    /**
     * 模板键。
     *
     * @since v7.260701.0
     */
    @TableField(value = "template_key")
    private String templateKey;

    /**
     * 当前信息点聚合的根对象 ID。
     *
     * @since v7.260701.0
     */
    @TableField(value = "object_id")
    private String objectId;

    /**
     * 信息点编码。
     *
     * @since v7.260701.0
     */
    @TableField(value = "info_code")
    private String infoCode;

    /**
     * 完整有序模板对象明细列表 JSON。
     *
     * <p>显式空列表保存为 {@code []}，与数据库中不存在对应记录的语义不同。</p>
     *
     * @since v7.260701.0
     */
    @TableField(value = "result_json")
    private String resultJson;
}
