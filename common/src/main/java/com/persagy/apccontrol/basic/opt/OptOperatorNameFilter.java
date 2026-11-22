package com.persagy.apccontrol.basic.opt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * opt 分支 B15（switch.opt.operatorNameLocal，默认关）：请求头带 {@value OptOperatorNames#HEADER} 时按 userId 记下操作人姓名。
 * 开关关闭或请求头不存在时直接放行，不做任何事；记录失败不影响请求。
 */
@Slf4j
@Component
public class OptOperatorNameFilter extends OncePerRequestFilter {

    private final OptOperatorNames operatorNames;

    public OptOperatorNameFilter(OptOperatorNames operatorNames) {
        this.operatorNames = operatorNames;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            if (OptSwitch.on(OptSwitch.OPERATOR_NAME_LOCAL)) {
                String name = request.getHeader(OptOperatorNames.HEADER);
                if (name != null && !name.isEmpty()) {
                    operatorNames.remember(request.getHeader("userId"), name);
                }
            }
        } catch (RuntimeException e) {
            log.debug("[opt-operator] 记录操作人姓名失败：{}", e.toString());
        }
        chain.doFilter(request, response);
    }
}
