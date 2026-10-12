package com.persagy.apccontrol.basic.utils;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.lang.NonNull;

import java.io.IOException;

/**
 * 项目工具类
 *
 * <ul>
 *     <li><b>getResourcePath</b> - 获取项目 [resources] 文件夹下的文件</li>
 * </ul>
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
public class ProjectUtils {

    private static final ResourcePatternResolver RESOLVER = new PathMatchingResourcePatternResolver();

    /**
     * 运行时获取项目中Resource
     *
     * @param filePath 相对于 {@code resource} 目录下的路径
     * @return 项目中Resource
     * @since v6.0.0
     */
    public static Resource getResource(@NonNull String filePath) {
        return RESOLVER.getResource(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + filePath);
    }

    /**
     * 运行时获取项目中Resource
     *
     * @param filePath 相对于 {@code resource} 目录下的路径
     * @return 项目中Resource
     * @since v6.0.0
     */
    public static Resource[] getResources(@NonNull String filePath) {
        try {
            return RESOLVER.getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + filePath);
        } catch (IOException e) {
            return new Resource[0];
        }
    }
}
