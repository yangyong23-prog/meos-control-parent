package com.persagy.apccontrol.config;

import com.persagy.apccontrol.interceptor.LoginTokenInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author shaohongbo
 * @date 2023年03月28日 22:41
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    @Bean
    public LoginTokenInterceptor loginTokenInterceptor() {
        return new LoginTokenInterceptor();
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginTokenInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/login")
                .excludePathPatterns("/saas/synuser")
                .excludePathPatterns("/3d/object/scene")
                .excludePathPatterns("/3d/object/meshes")
                .excludePathPatterns("/3d/object/version")
                .excludePathPatterns("/bizStaticType/getBasicsConfigType")
                .excludePathPatterns("/swagger/**", "/swagger-resources/**", "/webjars/**", "/v2/**", "/doc.html/**", "**/swagger-config")
                .excludePathPatterns("/strategyIntervention/chillerStrategy/queryByParam")
                .excludePathPatterns("/callback/time/strategy");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}
