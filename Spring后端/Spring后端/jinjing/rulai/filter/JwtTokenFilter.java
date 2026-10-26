package rulai.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import rulai.utils.JwtUtils;

import java.io.IOException;

/**
 * <p>
 * Condition Jwt过滤器，自动把Id写入Header
 * </p>
 *
 * @author Mistry
 * @since 2025-1-6
 */

@Component
@Order(1)
@Slf4j
public class JwtTokenFilter implements Filter {
    
    private static final String USER_ID_HEADER = "userId";
    private static final String TOKEN_HEADER = "token";
    
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, 
                        FilterChain filterChain) throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        
        String token = request.getHeader(TOKEN_HEADER);

        if (token != null && !token.trim().isEmpty()) {
            try {
                
                String userId = JwtUtils.getId(token);
                log.debug("从JWT提取用户ID: {}", userId);
                
                if (userId != null && !userId.trim().isEmpty()) {
                    MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest(request);
                    mutableRequest.putHeader(USER_ID_HEADER, userId);

                    filterChain.doFilter(mutableRequest, servletResponse);
                    return;
                }
            } catch (Exception e) {
                log.warn("JWT验证失败: {}", e.getMessage());
                return;
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);

//        MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest(request);
//        mutableRequest.putHeader(USER_ID_HEADER, "550e8400-e29b-41d4-a716-446655440001");
//        filterChain.doFilter(mutableRequest, servletResponse);
    }
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("JWT过滤器初始化");
    }
    
    @Override
    public void destroy() {
        log.info("JWT过滤器销毁");
    }
}