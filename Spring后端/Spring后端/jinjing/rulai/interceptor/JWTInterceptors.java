package rulai.interceptor;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import rulai.httpstatus.exceptions.UnauthorizedException;
import rulai.utils.JwtUtils;




import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
@Slf4j
public class JWTInterceptors implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
//        if (isAllowAnonymous(handler)) {
//            return true;
//        }
        Map<String,Object> map = new HashMap<>();
        // 获取请求头中令牌
        String token = request.getHeader("token");
        log.info("当前请求token:{}",token);
        try {
            // 验证令牌
            JwtUtils.verify(token);
            return true;
        } catch(Exception e){
            log.error("阻拦了一个请求");
            throw new UnauthorizedException("token有错误，请重新获取");
        }
    }
    /*private boolean isAllowAnonymous(Object handler) {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            // 检查方法上的注解
            Method method = handlerMethod.getMethod();
            if (method.isAnnotationPresent(AllowAnonymous.class)) {
                return true;
            }
            // 检查类上的注解
            Class<?> clazz = handlerMethod.getBeanType();
            return clazz.isAnnotationPresent(AllowAnonymous.class);
        }
        return false;
    }*/
}
