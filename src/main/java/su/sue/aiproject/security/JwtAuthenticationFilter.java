package su.sue.aiproject.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    // 白名单路径，这些路径不需要JWT认证
    private static final List<String> WHITELIST_PATHS = Arrays.asList(
        "/api/auth/login",
        "/api/auth/register", 
        "/doc.html",
        "/webjars/",
        "/swagger-resources/",
        "/v3/api-docs/"
    );

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        logger.debug("处理请求: {}", requestURI);
        
        // 检查是否是白名单路径
        if (isWhitelistPath(requestURI)) {
            logger.debug("白名单路径，跳过JWT验证: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            String jwt = getJwtFromRequest(request);

            if (!StringUtils.hasText(jwt)) {
                // 没有JWT token，直接返回401
                logger.warn("请求路径 {} 缺少JWT token", requestURI);
                sendUnauthorizedResponse(response, "缺少Authorization头或JWT token");
                return;
            }
            
            if (!tokenProvider.validateToken(jwt)) {
                // JWT token无效，直接返回401
                logger.warn("请求路径 {} JWT token无效", requestURI);
                sendUnauthorizedResponse(response, "JWT token无效或已过期");
                return;
            }
            
            // JWT有效，设置认证信息
            String username = tokenProvider.getUsernameFromJWT(jwt);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            logger.debug("JWT验证成功，用户: {}", username);
            
        } catch (Exception ex) {
            logger.error("JWT认证过程中发生错误: {}", ex.getMessage(), ex);
            sendUnauthorizedResponse(response, "认证失败: " + ex.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }
    
    /**
     * 检查是否是白名单路径
     */
    private boolean isWhitelistPath(String requestURI) {
        return WHITELIST_PATHS.stream()
                .anyMatch(path -> requestURI.startsWith(path));
    }
    
    /**
     * 发送401未授权响应
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        
        String jsonResponse = String.format(
            "{\"code\": 401, \"message\": \"%s\", \"data\": null, \"timestamp\": %d}",
            message, System.currentTimeMillis()
        );
        
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7, bearerToken.length());
        }
        return null;
    }
}
