package com.persagy.apccontrol.basic.outside.feign.wrapper;

import cn.hutool.core.bean.BeanUtil;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.dto.EnergyDataInfoQueryParam;
import com.persagy.apccontrol.basic.model.dto.EnergyDataQueryParam;
import com.persagy.apccontrol.basic.outside.dto.DmpResult;
import com.persagy.apccontrol.basic.outside.dto.QueryIPDataInPeriodDTO;
import com.persagy.apccontrol.basic.outside.feign.model.param.BasePageParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.exceptionevent.DangerEventQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.enums.ResponseCode;
import com.persagy.validate.utility.enumeration.error.client.RequestParamErrorEnum;
import com.persagy.validate.utility.exception.ApplicationException;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class FeignParamPageExecutor {

    public static final int DEFAULT_PAGE_SIZE = 100;

    public static <T> CustomerResponse<List<T>> execute(EnergyDataQueryParam param,
                                                        Function<EnergyDataQueryParam, CustomerResponse<List<T>>> executor) {
        List<EnergyDataInfoQueryParam> objects = param.getObjects();
        CustomerResponse<List<T>> result = new CustomerResponse<>();
        result.setResult(DmpResult.SUCCESS);
        result.setResultCode(ResponseCode.SUCCESS.getCode());
        if (CollectionUtils.isEmpty(objects)) {
            throw new ApplicationException(RequestParamErrorEnum.PARAMETER_EMPTY, LocaleMessageUtil.getMessage("key_97e5cfe68a814f359ea59ddb62ef075b"));
        }
        if (objects.size() <= 100) {
            return executor.apply(param);
        }
        List<List<EnergyDataInfoQueryParam>> partition = Lists.partition(objects, DEFAULT_PAGE_SIZE);
        List<T> all = new ArrayList<>();
        List<CompletableFuture<CustomerResponse<List<T>>>> futures = new ArrayList<>();
        for (List<EnergyDataInfoQueryParam> dmpObjectBodyDtos : partition) {
            EnergyDataQueryParam newParam = new EnergyDataQueryParam();
            BeanUtil.copyProperties(param, newParam);
            newParam.setObjects(dmpObjectBodyDtos);
            futures.add(CompletableFuture.supplyAsync(() -> executor.apply(newParam), BusinessThreadPoolExecutor.ttlExecutor()));
        }
        for (CompletableFuture<CustomerResponse<List<T>>> future : futures) {
            CustomerResponse<List<T>> join = future.join();
            List<T> content = join.getContent();
            if (CollectionUtils.isNotEmpty(content)) {
                all.addAll(content);
            }
        }
        result.setContent(all);
        result.setCount(all.size());
        return result;
    }

    public static <T> DmpResult<List<T>> execute(QueryIPDataInPeriodDTO param,
                                                 Function<QueryIPDataInPeriodDTO, DmpResult<List<T>>> executor) {
        List<QueryIPDataInPeriodDTO.ObjectInfo> objects = param.getObjects();
        if (CollectionUtils.isEmpty(objects)) {
            throw new ApplicationException(RequestParamErrorEnum.PARAMETER_EMPTY, LocaleMessageUtil.getMessage("key_97e5cfe68a814f359ea59ddb62ef075b"));
        }
        if (objects.size() <= 100) {
            return executor.apply(param);
        }
        DmpResult<List<T>> result = new DmpResult<>();
        result.setResult(DmpResult.SUCCESS);
        List<List<QueryIPDataInPeriodDTO.ObjectInfo>> partition = Lists.partition(objects, DEFAULT_PAGE_SIZE);
        List<T> all = new ArrayList<>();
        List<CompletableFuture<DmpResult<List<T>>>> futures = new ArrayList<>();
        for (List<QueryIPDataInPeriodDTO.ObjectInfo> dmpObjectBodyDtos : partition) {
            QueryIPDataInPeriodDTO newParam = new QueryIPDataInPeriodDTO();
            BeanUtil.copyProperties(param, newParam);
            newParam.setObjects(dmpObjectBodyDtos);
            futures.add(CompletableFuture.supplyAsync(() -> executor.apply(newParam), BusinessThreadPoolExecutor.ttlExecutor()));
        }
        for (CompletableFuture<DmpResult<List<T>>> future : futures) {
            DmpResult<List<T>> join = future.join();
            List<T> content = join.getData();
            if (CollectionUtils.isNotEmpty(content)) {
                all.addAll(content);
            }
        }
        result.setData(all);
        result.setCount(all.size());
        return result;
    }

    public static <T> CustomerResponse<List<T>> execute(DangerEventQueryParam param,
                                                        Function<DangerEventQueryParam, CustomerResponse<List<T>>> executor) {
        boolean findByRwdInstanceIds = true;
        List<String> rwdInstanceIds = param.getRwdInstanceIds();
        if (CollectionUtils.isEmpty(rwdInstanceIds)) {
            findByRwdInstanceIds = false;
            rwdInstanceIds = param.getRiskEventInstanceTreeIds();
        }
        CustomerResponse<List<T>> result = new CustomerResponse<>();
        result.setResult(DmpResult.SUCCESS);
        result.setResultCode(ResponseCode.SUCCESS.getCode());
        if (CollectionUtils.isEmpty(rwdInstanceIds)) {
            throw new ApplicationException(RequestParamErrorEnum.PARAMETER_EMPTY, LocaleMessageUtil.getMessage("key_97e5cfe68a814f359ea59ddb62ef075b"));
        }
        if (rwdInstanceIds.size() <= 1000) {
            return executor.apply(param);
        }
        List<List<String>> partition = Lists.partition(rwdInstanceIds, 1000);
        List<T> all = new ArrayList<>();
        List<CompletableFuture<CustomerResponse<List<T>>>> futures = new ArrayList<>();
        for (List<String> rwdInstanceId : partition) {
            DangerEventQueryParam newParam = new DangerEventQueryParam();
            if (findByRwdInstanceIds) {
                newParam.setRwdInstanceIds(rwdInstanceId);
            } else {
                newParam.setRiskEventInstanceTreeIds(rwdInstanceId);
            }
            futures.add(CompletableFuture.supplyAsync(() -> executor.apply(newParam), BusinessThreadPoolExecutor.ttlExecutor()));
        }
        for (CompletableFuture<CustomerResponse<List<T>>> future : futures) {
            CustomerResponse<List<T>> join = future.join();
            List<T> content = join.getContent();
            if (CollectionUtils.isNotEmpty(content)) {
                all.addAll(content);
            }
        }
        result.setContent(all);
        result.setCount(all.size());
        return result;
    }

    public static <T, F> CustomerResponse<List<T>> execute(BasePageParam<F> param, int limit,
                                                           Function<BasePageParam<F>, CustomerResponse<List<T>>> executor) {
        Collection<F> objects = param.getObjects();
        CustomerResponse<List<T>> result = new CustomerResponse<>();
        result.setResult(DmpResult.SUCCESS);
        result.setResultCode(ResponseCode.SUCCESS.getCode());
        if (CollectionUtils.isEmpty(objects)) {
            throw new ApplicationException(RequestParamErrorEnum.PARAMETER_EMPTY, LocaleMessageUtil.getMessage("key_97e5cfe68a814f359ea59ddb62ef075b"));
        }
        if (objects.size() <= limit) {
            return executor.apply(param);
        }
        List<List<F>> partitionList = Lists.partition(Lists.newArrayList(objects), limit);
        List<T> all = new ArrayList<>();
        List<CompletableFuture<CustomerResponse<List<T>>>> futures = new ArrayList<>();
        for (Collection<F> partitionIds : partitionList) {
            BasePageParam<F> newParam = new BasePageParam<>();
            newParam.setObjects(partitionIds);
            futures.add(CompletableFuture.supplyAsync(() -> executor.apply(newParam), BusinessThreadPoolExecutor.ttlExecutor()));
        }
        for (CompletableFuture<CustomerResponse<List<T>>> future : futures) {
            CustomerResponse<List<T>> join = future.join();
            List<T> content = join.getContent();
            if (CollectionUtils.isNotEmpty(content)) {
                all.addAll(content);
            }
        }
        result.setContent(all);
        result.setCount(all.size());
        return result;
    }


}
