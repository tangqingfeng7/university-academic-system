package com.university.academic.security;

import com.university.academic.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器
 * 从请求头中提取JWT令牌并验证，然后设置Spring Security上下文
 *
 * @author Academic System Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            // 从请求头中提取JWT令牌
            String jwt = extractJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && jwtUtil.validateToken(jwt)) {
                // 从令牌中获取用户名
                String username = jwtUtil.getUsernameFromToken(jwt);

                // 加载用户详情
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 验证令牌是否与用户匹配
                if (jwtUtil.validateToken(jwt, username)) {
                    // 创建认证对象
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    // 设置请求详情
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 设置到Spring Security上下文
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("已为用户 '{}' 设置认证信息", username);
                }
            }
        } catch (Exception e) {
            log.error("无法设置用户认证信息: {}", e.getMessage());
        }

        // 继续过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中提取JWT令牌
     *
     * @param request HTTP请求
     * @return JWT令牌，如果不存在则返回null
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }
}

