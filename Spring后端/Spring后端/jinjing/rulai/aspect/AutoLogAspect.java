package rulai.aspect;

import com.qcloud.cos.utils.Jackson;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import rulai.annotation.AutoLog;
import rulai.common.APIResponse;
import rulai.entity.OperationLog;
import rulai.service.IOperationLogService;
import rulai.service.ISessionService;
import rulai.utils.RequestContextUtil;
import rulai.utils.ContextUtil;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * <p>
 * 操作日志切面
 * 自动记录被@AutoLog注解标记的方法
 * </p>
 *
 * @author Mistry
 * @since 2026-01-06
 */

@Aspect
@Component
@Slf4j
public class AutoLogAspect {
    
    @Autowired
    private IOperationLogService operationLogService;

    @Lazy
    @Autowired
    private ISessionService sessionService;

    /**
     * 定义切点：所有被@AutoLog注解标记的方法
     */
    @Pointcut("@annotation(rulai.annotation.AutoLog)")
    public void autoLogPointCut() {
    }
    
    /**
     * 方法正常返回时的日志记录
     */
    @AfterReturning(pointcut = "autoLogPointCut()", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Object result) {
        try {
            saveLog(joinPoint, result, null);
        } catch (Exception e) {
            log.error("记录操作日志异常: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 方法抛出异常时的日志记录
     */
    @AfterThrowing(pointcut = "autoLogPointCut()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Throwable e) {
        try {
            saveLog(joinPoint, null, e.getMessage());
        } catch (Exception ex) {
            log.error("记录异常操作日志异常: {}", ex.getMessage(), ex);
        }
    }
    
    /**
     * 保存操作日志
     */
    private void saveLog(JoinPoint joinPoint, Object result, String errorMsg) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            
            AutoLog autoLog = method.getAnnotation(AutoLog.class);
            if (autoLog == null) {
                return;
            }

            HttpServletRequest request = RequestContextUtil.getRequest();
            String userId = ContextUtil.getUserId();
            String sessionId = ContextUtil.getSessionId();
            if (userId == null) {
                log.warn("无法获取用户ID，跳过操作日志记录。方法: {}.{}",
                        method.getDeclaringClass().getSimpleName(),
                        method.getName());
                return;
            }

            if (sessionId == null) {
                log.warn("无法获取会话ID，跳过操作日志记录。方法: {}.{}",
                        method.getDeclaringClass().getSimpleName(),
                        method.getName());
                return;
            }

            OperationLog operationLog = new OperationLog();
            operationLog.setId(UUID.randomUUID().toString());
            operationLog.setUserId(userId);
            operationLog.setType(autoLog.type());
            operationLog.setTableName(autoLog.tableName());
            
            if (request != null) {
                operationLog.setUrl(request.getRequestURL().toString());
                if (autoLog.recordRequest()) {
                    String requestParams = getRequestParams(joinPoint);
                    operationLog.setRequest(requestParams);
                }
            }
            
            if (autoLog.recordResponse() && result != null) {

                if(result instanceof APIResponse){
                    APIResponse apiResponse = (APIResponse) result;
                    try {
                        operationLog.setRespond(Jackson.toJsonString(apiResponse.getData()));
                    } catch (Exception e) {
                        operationLog.setRespond(apiResponse.getData().toString());
                    }
                } else {
                    try {
                        operationLog.setRespond(Jackson.toJsonString(result));
                    } catch (Exception e) {
                        operationLog.setRespond(result.toString());
                    }
                }
            }
            
            String description = autoLog.description();
            if (description == null || description.trim().isEmpty()) {
                description = String.format("执行方法: %s.%s", 
                    method.getDeclaringClass().getSimpleName(), 
                    method.getName());
            }
            
            if (errorMsg != null && !errorMsg.trim().isEmpty()) {
                description += " | 错误: " + errorMsg;
            }
            operationLog.setDescription(description);
            operationLog.setCreatedAt(LocalDateTime.now());
            operationLogService.saveAsync(operationLog);
            sessionService.addOperation(sessionId, operationLog.getId());
            
        } catch (Exception e) {
            log.error("构建操作日志失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 获取请求参数
     */
    private String getRequestParams(JoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args == null || args.length == 0) {
                return null;
            }
            
            Object[] filteredArgs = new Object[args.length];
            int count = 0;
            for (Object arg : args) {
                if (arg != null) {
                    String className = arg.getClass().getName();
                    if (!className.startsWith("javax.servlet") && 
                        !className.startsWith("org.springframework")) {
                        filteredArgs[count++] = arg;
                    }
                }
            }
            
            if (count == 0) {
                return null;
            }
            
            Object[] finalArgs = new Object[count];
            System.arraycopy(filteredArgs, 0, finalArgs, 0, count);
            
            return Jackson.toJsonString(finalArgs);
        } catch (Exception e) {
            log.warn("序列化请求参数失败: {}", e.getMessage());
            return null;
        }
    }
}