package com.persagy.apccontrol.basic.service.systemgraph;

import com.baomidou.mybatisplus.extension.service.IService;
import com.persagy.apccontrol.basic.model.entity.systemgraph.SystemGraphDuplicate;
import com.persagy.apccontrol.basic.model.enumtype.systemgraph.EnumGraphType;
import com.persagy.apccontrol.basic.model.enumtype.systemgraph.EnumSystemGraphStatus;
import com.persagy.apccontrol.basic.model.vo.systemgraph.SystemGraphDuplicateVO;
import com.persagy.apccontrol.basic.model.vo.systemgraph.SystemGraphInstanceVo;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author persagy
 * @description 针对表【system_graph_duplicate(系统图副本表)】的数据库操作Service
 * @createDate 2024-06-25 11:55:16
 */
public interface SystemGraphDuplicateService extends IService<SystemGraphDuplicate> {

    List<SystemGraphDuplicate> listSystemGraphDuplicate(String systemObjectId, String graphTypeCode, EnumSystemGraphStatus status);

    SystemGraphDuplicate saveDuplicate(String systemObjectId, EnumGraphType graphType,
                                       String classCode, String graphTypeCode, Long systemGraphDataId,
                                       Boolean isNotEdit, String generateId);

    SystemGraphDuplicateVO createSystemGraphDuplicate(Long duplicateId);

    Boolean publishSystemGraph(Long duplicateId);

    Boolean updateDuplicateDataById(Long duplicateId, Long systemGraphDataId);

    /**
     * 根据副本id更新名称
     *
     * @param duplicateId   副本id
     * @param duplicateName 副本名称
     * @return 操作结果
     */
    Boolean editDuplicateNameById(Long duplicateId, String duplicateName);

    Map<Long, SystemGraphInstanceVo> mapSystemGraphDataByDuplicateIds(@NotNull Collection<Long> duplicateIds);
}
