package com.persagy.apccontrol.basic.service.systemgraph;

import com.persagy.apccontrol.basic.model.entity.systemgraph.SystemGraphDetail;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author persagy
* @description 针对表【system_graph_detail(系统图细节调整表)】的数据库操作Service
* @createDate 2024-06-25 11:55:03
*/
public interface SystemGraphDetailService extends IService<SystemGraphDetail> {

    List<SystemGraphDetail> listSystemGraphDetailByDuplicateId(Long duplicateId);
}
