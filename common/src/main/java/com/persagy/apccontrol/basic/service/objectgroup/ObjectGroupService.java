package com.persagy.apccontrol.basic.service.objectgroup;

import com.baomidou.mybatisplus.extension.service.IService;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.entity.objectgroup.ObjectGroup;
import com.persagy.apccontrol.basic.model.param.objectgroup.ObjectGroupParam;
import com.persagy.apccontrol.basic.model.param.objectgroup.QueryObjectGroupNameParam;
import com.persagy.apccontrol.basic.model.param.objectgroup.QueryObjectGroupParam;
import com.persagy.apccontrol.basic.model.vo.objectgroup.ObjectGroupVO;
import org.jspecify.annotations.NonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

public interface ObjectGroupService extends IService<ObjectGroup>, LogComponent {


    /**
     * 查询组
     *
     * @param param 查询参数
     * @return {@code List<ObjectGroupVO> }
     * @since 7.250402.0
     */
    List<ObjectGroupVO> queryGroup(@NotNull QueryObjectGroupParam param);

    /**
     * 是否存在编组名
     *
     * @param param 参选参数
     * @return {@code Boolean }
     * @since 7.250402.0
     */
    Boolean hasExistsGroupName(@NotNull QueryObjectGroupNameParam param);

    /**
     * 删除编组
     *
     * @param id     编组id
     * @param isEdge
     * @return {@code Boolean }
     * @since 7.250402.0
     */
    Boolean deleteGroup(@NotBlank Long id, Boolean isEdge);

    /**
     * 编辑编组
     *
     * @param param  参数
     * @param isEdge
     * @return {@code Boolean }
     * @since 7.250402.0
     */
    Boolean editGroup(@NonNull ObjectGroupParam param, Boolean isEdge);
}
