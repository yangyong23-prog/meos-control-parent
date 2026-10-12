package com.persagy.apccontrol.basic.resource.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.persagy.apccontrol.basic.mapper.RcEqInfoCodeCfgMapper;
import com.persagy.apccontrol.basic.model.dto.system.RcEqInfoCodeCfgDTO;
import com.persagy.apccontrol.basic.model.entity.RcEqInfoCodeCfg;
import com.persagy.apccontrol.basic.model.vo.system.RcEqInfoCodeCfgVO;
import com.persagy.apccontrol.basic.resource.RcEqInfoCodeCfgService;
import com.persagy.framework.database.service.SuperServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author yuyao
 * @description 针对表【rc_eq_info_code_cfg(设备信息点配置 已经绑定的设备信息通过rc_info_code_cfg配置生成)】的数据库操作Service实现
 * @createDate 2023-03-15 20:30:22
 */
@Service
public class RcEqInfoCodeCfgServiceImpl extends SuperServiceImpl<RcEqInfoCodeCfgMapper, RcEqInfoCodeCfg>
        implements RcEqInfoCodeCfgService {

    @Resource
    private RcEqInfoCodeCfgMapper rcEqInfoCodeCfgMapper;

    @Override
    public void synPic(RcEqInfoCodeCfg cfg) {
        //添加数据
        cfg.setCreateTime(DateUtil.dateSecond());
        cfg.setSysTime(DateUtil.dateSecond());
        save(cfg);

    }

    /**
     * 查询设备信息关系表
     * @author tangzy
     * @param dto
     * @return java.util.List<com.persagy.apccontrol.model.vo.system.RcEqInfoCodeCfgVO>
     **/
    @Override
    public List<RcEqInfoCodeCfgVO> queryRcEqInfoCodeCfgPage(RcEqInfoCodeCfgDTO dto) {
        LambdaQueryWrapper<RcEqInfoCodeCfg> wrapper = Wrappers.<RcEqInfoCodeCfg>query().lambda()
                .eq(ObjectUtil.isNotEmpty(dto.getEquipmentId()), RcEqInfoCodeCfg::getEquipmentId, dto.getEquipmentId())
                .eq(ObjectUtil.isNotEmpty(dto.getParameterType()), RcEqInfoCodeCfg::getParameterType, dto.getParameterType())
                .eq(ObjectUtil.isNotEmpty(dto.getProjectId()), RcEqInfoCodeCfg::getProjectId, dto.getProjectId())
                .eq(ObjectUtil.isNotEmpty(dto.getInfoCode()), RcEqInfoCodeCfg::getInfoCode, dto.getInfoCode())
                .eq(ObjectUtil.isNotEmpty(dto.getClassCode()), RcEqInfoCodeCfg::getClassCode, dto.getClassCode())
                .eq(ObjectUtil.isNotEmpty(dto.getSystemId()), RcEqInfoCodeCfg::getSystemId, dto.getSystemId())
                .orderByDesc(RcEqInfoCodeCfg::getOrdered);
        List<RcEqInfoCodeCfg> list = this.list(wrapper);
        return BeanUtil.copyToList(list, RcEqInfoCodeCfgVO.class);
    }

    @Override
    public void saveRcEqInfoCodeCfg(List<RcEqInfoCodeCfg> synRcEqInfoCodeCfg) {
        if (CollectionUtil.isEmpty(synRcEqInfoCodeCfg)) {
            return;
        }

        //先删除后添加
        rcEqInfoCodeCfgMapper.delete(new QueryWrapper<RcEqInfoCodeCfg>()
                .lambda()
                .eq(RcEqInfoCodeCfg::getProjectId, synRcEqInfoCodeCfg.get(0).getProjectId())
        );
        synRcEqInfoCodeCfg.forEach(i -> {
            i.setId(Convert.toStr(IdUtil.getSnowflakeNextId()));
            i.setCreateTime(DateUtil.dateSecond());
            i.setSysTime(DateUtil.dateSecond());

        });
        rcEqInfoCodeCfgMapper.insertBatch(synRcEqInfoCodeCfg);
        //添加数据
        //saveBatch(synRcEqInfoCodeCfg);
    }

}




