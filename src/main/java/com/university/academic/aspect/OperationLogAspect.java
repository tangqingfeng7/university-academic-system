package com.university.academic.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.university.academic.annotation.OperationLog;
import com.university.academic.repository.OperationLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 操作日志切面
 * 自动记录标注了@OperationLog注解的方法的操作日志
 *
 * @author Academic System Team
 */
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private static final org.slf4j.Logger logger = 
            org.slf4j.LoggerFactory.getLogger(OperationLogAspect.class);

    private final OperationLogRepository logRepository;
    private final ObjectMapper objectMapper;

    /**
     * 环绕通知，记录操作日志
     */
    @Around("@annotation(com.university.academic.annotation.OperationLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // 获取注解
        OperationLog operationLog = method.getAnnotation(OperationLog.class);
        
        // 构建日志对象
        com.university.academic.entity.OperationLog log = 
                com.university.academic.entity.OperationLog.builder()
                .operation(operationLog.value())
                .method(joinPoint.getSignature().getDeclaringTypeName() + "." + 
                       joinPoint.getSignature().getName())
                .createdAt(LocalDateTime.now())
                .build();

        // 获取用户信息
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                !"anonymousUser".equals(authentication.getPrincipal())) {
                log.setUsername(authentication.getName());
                // 这里可以根据需要获取userId
            }
        } catch (Exception e) {
            logger.error("获取用户信息失败", e);
        }

        // 获取请求参数
        try {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                // 过滤掉不需要序列化的参数（如HttpServletRequest等）
                Object[] filteredArgs = new Object[args.length];
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof HttpServletRequest || 
                        args[i] instanceof Authentication) {
                        filteredArgs[i] = args[i].getClass().getSimpleName();
                    } else {
                        filteredArgs[i] = args[i];
                    }
                }
                String params = objectMapper.writeValueAsString(filteredArgs);
                // 限制参数长度
                if (params.length() > 2000) {
                    params = params.substring(0, 2000) + "...";
                }
                log.setParams(params);
            }
        } catch (Exception e) {
            log.setParams("参数序列化失败: " + e.getMessage());
            logger.error("记录请求参数失败", e);
        }

        // 获取IP地址
        try {
            ServletRequestAttributes attributes = 
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                log.setIp(getIpAddress(request));
            }
        } catch (Exception e) {
            logger.error("获取IP地址失败", e);
        }

        // 执行方法并记录执行时间
        long startTime = System.currentTimeMillis();
        Object result = null;
        try {
            result = joinPoint.proceed();
            log.setStatus(com.university.academic.entity.OperationLog.LogStatus.SUCCESS);
            return result;
        } catch (Throwable e) {
            log.setStatus(com.university.academic.entity.OperationLog.LogStatus.FAILURE);
            log.setErrorMsg(e.getMessage());
            if (log.getErrorMsg() != null && log.getErrorMsg().length() > 2000) {
                log.setErrorMsg(log.getErrorMsg().substring(0, 2000) + "...");
            }
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();
            log.setExecutionTime(endTime - startTime);
            
            // 异步保存日志（避免影响主业务）
            try {
                logRepository.save(log);
            } catch (Exception e) {
                logger.error("保存操作日志失败", e);
            }
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 对于通过多个代理的情况，第一个IP为客户端真实IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}

