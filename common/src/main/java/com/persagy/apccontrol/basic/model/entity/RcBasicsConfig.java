package com.persagy.apccontrol.basic.model.entity;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.model.dto.SynDTO;
import com.persagy.apccontrol.basic.model.vo.cfg.BasicsConfigVO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import java.time.LocalDateTime;

/**
 * 基础配置表
 *
 * @author yuyao
 * @TableName rc_basics_config
 */
@TableName(value = "rc_basics_config")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class RcBasicsConfig extends SynDTO {

    public static final String LOG_TAG = "基础配置";
    public static final String TABLE_NAME = "rc_basics_config";

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId("id")
    private String id;
    /**
     * 项目ID
     */
    private String projectId;
    /**
     * 配置类型
     */
    private BasicsConfigTypeEnum type;
    /**
     * 子配置类型
     */
    private String childType;
    /**
     * 配置状态(有效无效)
     */
    private StateTypeEnum st;
    /**
     * 建筑编码
     */
    private String architectureCode;
    /**
     * 楼层编码
     */
    private String floorCode;
    /**
     * 房间编码
     */
    private String roomCode;
    /**
     * 设备编码
     */
    private String facilityCode;

    /**
     * 系统编号
     */
    private String systemId;
    /**
     * 表达式
     */
    private String spEl;
    /**
     * 配置生效时间
     */
    private LocalDateTime startTime;
    /**
     * 配置失效时间
     */
    private LocalDateTime endTime;
    /**
     * 配置结果MD5(配置结果的MD5用于配置幂等判断)
     */
    private String md;
    /**
     * 配置结果
     */
    private String result;
    /**
     * 扩展信息(额外备用参数配置信息)
     */
    private String extendInfo;
    /**
     * 配置备注
     */
    private String memo;
    /**
     * 创建人
     */
    private String createUser;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 修改人
     */
    private String updateUser;
    /**
     * 修改时间
     */
    private LocalDateTime updateTime;
    /**
     * 同步时间
     */
    private LocalDateTime synTime;

    /**
     * 复制一个新实例
     *
     * @return 新实例
     * @since v5.1.0
     */
    public RcBasicsConfig copyNewModel() {
        RcBasicsConfig result = BeanUtil.copyProperties(this, RcBasicsConfig.class);
        result.setId(null);
        result.setCreateTime(null);
        result.setCreateUser(null);
        result.setUpdateTime(null);
        result.setUpdateUser(null);
        result.setSynTime(null);
        return result;
    }

    /**
     * 设置md5数据
     *
     * @param vo vo
     */
    public <T extends BasicsConfigVO> void setMd5(T vo) {
        if (vo == null) {
            return;
        }
        this.md = DigestUtils.md5Hex(vo.toString());
    }


    /**
     * 设置md5数据
     */
    public void setMd5ByResult() {
        if (result == null) {
            return;
        }
        this.md = DigestUtils.md5Hex(result);
    }
}