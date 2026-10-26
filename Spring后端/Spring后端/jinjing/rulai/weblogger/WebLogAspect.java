package rulai.weblogger;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * <p>
 * 日志记录切面
 * </p>
 *
 * @author Mistry
 * @since 2025-06-23
 */

@Aspect
public class WebLogAspect {

    private final static Logger logger = LoggerFactory.getLogger(WebLogAspect.class);

    @Pointcut("@annotation(rulai.weblogger.WebLog)")
    public void pointcut() {
    }

    /**
     * 定义 @Around 环绕
     * @param proceedingJoinPoint
     * @return
     * @throws Throwable
     */
    @Around("pointcut()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        logger.info("@Around");
        long startTime = System.currentTimeMillis();
        Object result = proceedingJoinPoint.proceed();
        String logOutput = result.toString();
        logger.info("Response Args  : {}", logOutput.length()>1024?logOutput.substring(0, 1024)+"...":logOutput);
        logger.info("Time-Consuming : {} ms", System.currentTimeMillis() - startTime);
        return result;
    }

    /**
     * 在切点之前织入
     * @param joinPoint
     * @throws Throwable
     */
    @Before("pointcut()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String methodDescription = getAspectLogDescription(joinPoint);

        logger.info("===================================== Start =====================================");
        logger.info("URL            : {}", request.getRequestURL().toString());
        logger.info("Description    : {}", methodDescription);
        logger.info("HTTP Method    : {}", request.getMethod());
        logger.info("Class Method   : {}.{}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
        logger.info("IP             : {}", request.getRemoteAddr());
        logger.info("Request Args   : {}", Arrays.toString(joinPoint.getArgs()));
    }

    /**
     * 在切点之后织入
     * @throws Throwable
     */
    @After("pointcut()")
    public void doAfter() throws Throwable {
        logger.info("====================================== End ======================================");
    }

    /**
     * 获取切面注解的描述
     * @param joinPoint
     * @return
     * @throws Exception
     */
    public String getAspectLogDescription(JoinPoint joinPoint) throws Exception {
        String targetName = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        Object[] arguments = joinPoint.getArgs();
        Class targetClass = Class.forName(targetName);
        Method[] methods = targetClass.getMethods();
        StringBuilder description = new StringBuilder("");
        for (Method method : methods){
            if (method.getName().equals(methodName)){
                Class[] clazzs = method.getParameterTypes();
                if (clazzs.length == arguments.length){
                    description.append(method.getAnnotation(WebLog.class).description());
                    break;
                }
            }
        }
        return description.toString();
    }
}
