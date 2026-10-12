package com.persagy.apccontrol.basic.service.systemgraph.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.persagy.apccontrol.basic.model.entity.systemgraph.SystemGraphDetail;
import com.persagy.apccontrol.basic.service.systemgraph.SystemGraphDetailService;
import com.persagy.apccontrol.basic.mapper.SystemGraphDetailMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author persagy
 * @description 针对表【system_graph_detail(系统图细节调整表)】的数据库操作Service实现
 * @createDate 2024-06-25 11:55:03
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SystemGraphDetailServiceImpl extends ServiceImpl<SystemGraphDetailMapper, SystemGraphDetail>
        implements SystemGraphDetailService {

    @Override
    public List<SystemGraphDetail> listSystemGraphDetailByDuplicateId(Long duplicateId) {
        LambdaQueryWrapper<SystemGraphDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemGraphDetail::getSystemGraphDuplicateId, duplicateId);
        return this.list(wrapper);
    }
}




