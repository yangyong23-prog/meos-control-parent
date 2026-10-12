package com.persagy.apccontrol.basic.utils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.outside.dto.DmpResult;
import com.persagy.framework.common.enums.ResponseCode;
import com.persagy.framework.common.exception.BusinessException;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 时间相关的工具类
 *
 * @author duanruixiang
 */
public class DMPResultUtil {


    @SuppressWarnings("rawtypes")
	public static <T> List<T> getList(DmpResult result, Class<T> clazz) throws Exception {
        ArrayNode jsonNode = (ArrayNode) result.getData();

        if (jsonNode == null || jsonNode.isEmpty()) {
            return new ArrayList<T>();
        }
        return CommonConst.JSON_MAPPER.readValue(jsonNode.toString(), new TypeReference<List<T>>() {
        });
    }

    public static <T> void handleResult(DmpResult<T> result) {
        String result1 = result.getResult();
        if (StringUtils.isBlank(result1) || !DmpResult.SUCCESS.equals(result1)) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), result.getMessage() == null ? LocaleMessageUtil.getMessage("key_92cd29bb9b114e049ec56c1141b79bbd") : result.getMessage());
        }
    }


    /**
     * 拷贝基础信息
     *
     * @param result 数据中台结果
     * @return 只要基础信息，不要返回的数据
     * @author lixing
     * @version V1.0 2021/11/18 8:47 下午
     */
    private static <T> DmpResult<T> copyBasicInfo(DmpResult result) {
        DmpResult<T> tmp = new DmpResult<>();
        tmp.setResult(result.getResult());
        tmp.setCount(result.getCount());
        tmp.setMessage(result.getMessage());
        return tmp;
    }


}
