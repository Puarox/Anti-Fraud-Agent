package rulai.interceptor;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import rulai.httpstatus.exceptions.ForbiddenException;
import rulai.utils.JwtUtils;
import rulai.annotation.Dev_Permission;
import rulai.annotation.PERMISSION;


import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static rulai.utils.PermissionUtils.PERMISSION_MAP;

@Slf4j
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            // 如果不是HandlerMethod类型，直接放行
            if (!(handler instanceof HandlerMethod)) {
                return true;
            }

            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            // 1. 获取类上的注解
            boolean has_dev_class_annotation = handlerMethod.getBeanType().isAnnotationPresent(Dev_Permission.class);
            // 2. 获取方法上的注解
            boolean has_dev_method_annotation = method.isAnnotationPresent(Dev_Permission.class);
            //如果有dev标志，那么直接放行
            if(has_dev_class_annotation || has_dev_method_annotation) { return true ;}


            // 收集所有需要的权限
            Set<String> requiredPermissions = new HashSet<>();

            // 1. 获取类上的注解
            PERMISSION classAnnotation = handlerMethod.getBeanType().getAnnotation(PERMISSION.class);
            if (classAnnotation != null) {
                requiredPermissions.addAll(Arrays.asList(classAnnotation.permissions()));
            }

            // 2. 获取方法上的注解
            PERMISSION methodAnnotation = method.getAnnotation(PERMISSION.class);
            if (methodAnnotation != null) {
                requiredPermissions.addAll(Arrays.asList(methodAnnotation.permissions()));
            }

            // 如果没有权限要求，直接放行
            if (requiredPermissions.isEmpty()) {
                return true;
            }

            // 3. 检查当前用户是否拥有所需权限
            if (!hasAllPermissions(request, requiredPermissions)) {
           //     response.setStatus(HttpServletResponse.SC_FORBIDDEN);
             //   response.getWriter().write("{\"code\":403,\"message\":\"forbidden!no enough permissions...\"}");
                throw new ForbiddenException("没有权限登录");

            }

            return true;
        }

        /**
         * 检查用户是否拥有所有需要的权限
         */
        private boolean hasAllPermissions(HttpServletRequest request, Set<String> requiredPermissions) {
            // 获取并验证token
            String token = request.getHeader("token");
            DecodedJWT verify = JwtUtils.verify(token);

            // 解析权限码
            int permissionsCode = Integer.parseInt(verify.getClaim("privilege").asString());
            Set<String> userPermissions = new HashSet<>();

            // 遍历权限掩码映射表，检查每个权限位
            for (Map.Entry<Integer, String> entry : PERMISSION_MAP.entrySet()) {
                int mask = entry.getKey();
                String permissionName = entry.getValue();

                // 检查权限码是否包含当前权限
                if ((permissionsCode & mask) != 0) {
                    userPermissions.add(permissionName);
                    log.info("当前权限： " + permissionName);
                }
            }

            // 检查用户权限是否包含所有需要的权限
            return userPermissions.containsAll(requiredPermissions);
        }
    }

