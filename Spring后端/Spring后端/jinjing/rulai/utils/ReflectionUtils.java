package rulai.utils;

import org.apache.commons.lang3.reflect.FieldUtils;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


public abstract class ReflectionUtils implements Serializable {
    private static final Map<Class<?>, List<Field>> FIELD_CACHE = new ConcurrentHashMap<>();
    
    public static boolean isEmpty(Object object) {
        List<Field> fields = FIELD_CACHE.computeIfAbsent(object.getClass(), clazz ->
            FieldUtils.getAllFieldsList(clazz).stream()
                .filter(f -> !Modifier.isStatic(f.getModifiers()))
                .peek(f -> f.setAccessible(true))
                .collect(Collectors.toList()));
        
        for (Field field : fields) {
            try {
                Object value = field.get(object);
                if (isNonEmpty(value)) {
                    return false;
                }
            } catch (IllegalAccessException e) {
                // 忽略访问异常
            }
        }
        return true;
    }

    public static void update(Object old, Object upd) {

        if(old == null) {
            throw new RuntimeException("源对象为null");
        }

        List<Field> fields = FIELD_CACHE.computeIfAbsent(old.getClass(), clazz ->
                FieldUtils.getAllFieldsList(clazz).stream()
                        .filter(f -> !Modifier.isStatic(f.getModifiers()))
                        .peek(f -> f.setAccessible(true))
                        .collect(Collectors.toList()));

        for (Field field : fields) {
                try {
                    if(field.get(upd) != null) {
                        field.set(old, field.get(upd));
                    }
                } catch (IllegalAccessException e) {
                    // 忽略
                }
        }
    }
    
    private static boolean isNonEmpty(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof CharSequence) {
            return !((CharSequence) value).isEmpty();
        }
        if (value instanceof Collection) {
            return !((Collection<?>) value).isEmpty();
        }
        if (value instanceof Map) {
            return !((Map<?, ?>) value).isEmpty();
        }
        if (value.getClass().isArray()) {
            return Array.getLength(value) > 0;
        }
        return true;
    }
}