package rulai.utils.interpolation;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import rulai.annotation.InterpolationInterface;

import jakarta.annotation.PostConstruct;
import rulai.constant.INTERFACE_CONSTANT;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 注册表管理器，用于存储和调用注册的接口
 */
@Component
public class InterfaceRegistry implements ApplicationContextAware {
    
    private static final Map<String, Function<Object[], Object>> registry = new ConcurrentHashMap<>();
    private static final Map<String, String> typeRegistry = new ConcurrentHashMap<>();

    @Autowired
    private ApplicationContext applicationContext;
    
    private static ApplicationContext staticApplicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        staticApplicationContext = applicationContext;
    }
    
    /**
     * 初始化注册表，扫描所有带有@InterpreterInterface注解的方法
     */
    @PostConstruct
    public void initRegistry() {
        // 扫描@Service注解的类
        Map<String, Object> serviceBeans = applicationContext.getBeansWithAnnotation(org.springframework.stereotype.Service.class);
        for (Object bean : serviceBeans.values()) {
            ReflectionUtils.doWithMethods(bean.getClass(), method -> {
                InterpolationInterface annotation = AnnotationUtils.findAnnotation(method, InterpolationInterface.class);
                if (annotation != null) {
                    registerMethod(annotation, bean, method);
                }
            });
        }
    }
    
    /**
     * 注册方法到注册表
     */
    private void registerMethod(InterpolationInterface annotation, Object bean, Method method) {
        String interfaceName = annotation.value();
        if (interfaceName == null || interfaceName.isEmpty()) {
            interfaceName = method.getName();
        }
        
        // 创建函数式接口包装方法调用
        String finalInterfaceName = interfaceName;
        Function<Object[], Object> function = (arg) -> {
            try {
                Object []invokeArgs = new Object[method.getParameterCount()];
                for (int i = 0; i < Math.min(arg.length, invokeArgs.length); i++) {
                    invokeArgs[i] = arg[i];
                }
                method.setAccessible(true);
                return method.invoke(bean, invokeArgs);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("调用注册方法失败: " + finalInterfaceName, e);
            }
        };
        String type = annotation.type().name();

        if (registry.containsKey(interfaceName)) {
            throw new IllegalStateException("接口名称重复: " + interfaceName);
        }
        
        registry.put(interfaceName, function);
        typeRegistry.put(interfaceName, type);
        System.out.println("成功注册接口: " + interfaceName + " -> " + bean.getClass().getSimpleName() + "." + method.getName());
    }
    
    /**
     * 检查接口是否已注册
     */
    public static boolean isRegistered(String interfaceName) {
        System.out.println("interfaceName = " + interfaceName);
        return registry.containsKey(interfaceName);
    }


    /**
     * 检查接口是否已注册
     */
    public static Function<Object[], Object> getInterface(String interfaceName) {
        return registry.get(interfaceName);
    }

    /**
     * 检查接口是否已注册
     */
    public static String getInterfaceType(String interfaceName) {
        return typeRegistry.get(interfaceName);
    }
    
    /**
     * 获取所有注册的接口名称
     */
    public static Map<String, String> getRegisteredInterfaces() {
        Map<String, String> result = new HashMap<>();
        
        if (staticApplicationContext != null) {
            Map<String, Object> allBeans = staticApplicationContext.getBeansWithAnnotation(
                    org.springframework.stereotype.Component.class);
            
            for (Map.Entry<String, Object> entry : allBeans.entrySet()) {
                Object bean = entry.getValue();
                ReflectionUtils.doWithMethods(bean.getClass(), method -> {
                    InterpolationInterface annotation = AnnotationUtils.findAnnotation(method, InterpolationInterface.class);
                    if (annotation != null) {
                        String interfaceName = annotation.value();
                        String displayName = annotation.displayName();
                        if (interfaceName == null || interfaceName.isEmpty()) {
                            interfaceName = method.getName();
                        }
                        result.put(interfaceName, displayName);
                    }
                });
            }
        }
        
        return result;
    }
    
    /**
     * 手动注册接口
     */
    public static void register(String interfaceName, Function<Object[], Object> function) {
        if (registry.containsKey(interfaceName)) {
            throw new IllegalStateException("接口已存在: " + interfaceName);
        }
        registry.put(interfaceName, function);
    }
    
    /**
     * 移除注册的接口
     */
    public static void unregister(String interfaceName) {
        registry.remove(interfaceName);
    }
}