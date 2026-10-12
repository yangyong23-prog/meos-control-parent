package com.persagy.apccontrol.basic.mapper;

import com.persagy.apccontrol.basic.model.entity.RcSystemPic;
import com.persagy.apccontrol.basic.model.enumtype.system.PictureTypeEnum;
import com.persagy.framework.database.mapper.SuperMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author yuyao
 * @description 针对表【rc_system_pic(系统图表)】的数据库操作Mapper
 * @createDate 2023-03-15 20:30:22
 * @Entity generator.domain.RcSystemPic
 */
@Mapper
@Repository
public interface RcSystemPicMapper extends SuperMapper<RcSystemPic> {

    public List<RcSystemPic> querySyn(@Param("projectId") String projectId, @Param("type") PictureTypeEnum type);
}




