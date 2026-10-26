package rulai.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 用户上下文工具类
 * 从请求头中获取用户ID
 */
@Component
@Slf4j
public class ContextUtil {
    
    private static final String USER_ID_HEADER = "userId";
    private static final String SESSION_ID_HEADER = "sessionId";

    /**
     * 获取当前登录用户ID
     * @return 用户ID，如果不存在则返回null
     */
    public static String getUserId() {
        HttpServletRequest request = RequestContextUtil.getRequest();
        if (request == null) {
            log.warn("无法获取HttpServletRequest，可能不在Web线程上下文中");
            return null;
        }
        
        String userId = request.getHeader(USER_ID_HEADER);
        
        if (userId == null || userId.trim().isEmpty()) {
            log.warn("请求头中未找到用户ID，请检查请求头是否包含: {}", USER_ID_HEADER);
            return null;
        }
        
        return userId.trim();
    }

    /**
     * 获取当前会话ID
     * @return 会话ID，如果不存在则返回null
     */
    public static String getSessionId() {
        HttpServletRequest request = RequestContextUtil.getRequest();
        if (request == null) {
            log.warn("无法获取HttpServletRequest，可能不在Web线程上下文中");
            return null;
        }
        String sessionId = request.getHeader(SESSION_ID_HEADER);
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return null;
        }
        return sessionId.trim();
    }
}