package com.persagy.apccontrol.basic.resource.impl;

import cn.hutool.core.util.StrUtil;
import com.persagy.apccontrol.basic.mapper.RcInfoCodeCfgMapper;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.resource.RcInfoCodeCfgService;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.database.service.SuperServiceImpl;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yuyao
 * @description 针对表【rc_info_code_cfg(信息点配置 )】的数据库操作Service实现
 * @createDate 2023-03-15 20:30:22
 */
@Service
public class RcInfoCodeCfgServiceImpl extends SuperServiceImpl<RcInfoCodeCfgMapper, RcInfoCodeCfg> implements RcInfoCodeCfgService {

    @Override
    public List<RcInfoCodeCfg> queryAllInfoCodeConfigs(String classCode) {
        return this.lambdaQuery().eq(StrUtil.isNotBlank(classCode), RcInfoCodeCfg::getClassCode, classCode).list();
    }

    @Override
    public Map<String, RcInfoCodeCfg> queryAllDataSourceAndUnit(@NotEmpty Set<String> classCode) {
        Map<String, RcInfoCodeCfg> map = new HashMap<>();
        //记录原编码
        String groupCode = ContextUtil.getGroupCode();
        //加锁 避免并行情况集团编码错乱
        synchronized (this) {
            ContextUtil.setGroupCode("");
            ContextUtil.setTenant("");
            try {
                List<RcInfoCodeCfg> list = this.lambdaQuery()
                        .select(RcInfoCodeCfg::getClassCode, RcInfoCodeCfg::getInfoCode, RcInfoCodeCfg::getDataSource, RcInfoCodeCfg::getUnit)
                        .in(RcInfoCodeCfg::getClassCode, classCode)
                        .and(wrapper -> wrapper
                                .isNotNull(RcInfoCodeCfg::getDataSource)
                                .or()
                                .isNotNull(RcInfoCodeCfg::getUnit)
                        )
                        .list();

                if (list.isEmpty()) {
                    return map;
                }
                //转换成 MAP 结构 方便查询
                list.forEach(i -> map.put(i.getClassCode() + i.getInfoCode(), i));

            } finally {
                //恢复集团编码
                ContextUtil.setGroupCode(groupCode);
                ContextUtil.setTenant(groupCode);
            }
        }
        return map;
    }

}




