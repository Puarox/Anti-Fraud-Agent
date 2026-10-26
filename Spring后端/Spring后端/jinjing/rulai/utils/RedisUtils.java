package rulai.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
/**
 * Redis 工具类
 *
 * 使用示例：
 *
 * 1. 基础操作：
 *    set("user:1", user, 30, TimeUnit.MINUTES)  // 设置缓存，30分钟过期
 *    User user = get("user:1", User.class)      // 获取缓存
 *    delete("user:1")                           // 删除缓存
 *    hasKey("user:1")                           // 检查key是否存在
 *
 * 2. 便捷方法：
 *    setObject("key", value)                    // 设置缓存，默认30分钟
 *    getOrSet("key", () -> loadFromDB())        // 获取或设置（防缓存穿透）
 *
 * 3. 类型转换：
 *    - 自动处理 GenericJackson2JsonRedisSerializer 的 LinkedHashMap
 *    - 支持自定义对象、字符串、列表等
 *
 * 4. 注意事项：
 *    - 存储的对象需要有默认构造函数
 *    - 建议为key添加业务前缀，如 "user:detail:{id}"
 *    - 使用 getOrSet 可防止缓存穿透
 */
@Component
@Slf4j
public class RedisUtils {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisUtils(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    // ====================== 基础操作 ======================

    /**
     * 设置缓存
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置缓存（带过期时间）
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 获取缓存
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 获取缓存并转换为指定类型
     */
    public <T> T get(String key, Class<T> clazz) {
        Object value = get(key);
        if (value == null) {
            return null;
        }

        try {
            // 如果是目标类型，直接返回
            if (clazz.isInstance(value)) {
                return clazz.cast(value);
            }

            // 如果是LinkedHashMap，尝试转换
            if (value instanceof LinkedHashMap) {
                return convertMapToObject((LinkedHashMap<?, ?>) value, clazz);
            }

            log.warn("Redis 数据类型不匹配, key: {}, 期望: {}, 实际: {}",
                    key, clazz.getName(), value.getClass().getName());
            return null;

        } catch (Exception e) {
            log.error("Redis 数据转换失败, key: {}", key, e);
            return null;
        }
    }

    /**
     * 删除缓存
     */
    public boolean delete(String key) {
        Boolean result = redisTemplate.delete(key);
        return Boolean.TRUE.equals(result);
    }

    /**
     * 检查key是否存在
     */
    public boolean hasKey(String key) {
        Boolean result = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(result);
    }

    /**
     * 设置过期时间
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        Boolean result = redisTemplate.expire(key, timeout, unit);
        return Boolean.TRUE.equals(result);
    }

    // ====================== 高级封装 ======================

    /**
     * 获取或设置缓存（防止缓存穿透）
     * 如果缓存不存在，从loader加载并设置缓存
     */
    public <T> T getOrSet(String key, Supplier<T> loader, long timeout, TimeUnit unit) {
        // 1. 先尝试从缓存获取
        T cached = get(key, (Class<T>) Object.class);
        if (cached != null) {
            return cached;
        }

        // 2. 缓存未命中，从loader加载
        T value = loader.get();
        if (value != null) {
            // 3. 设置缓存
            set(key, value, timeout, unit);
        } else {
            // 防止缓存穿透：存储空值
            set(key, new NullValue(), Math.min(timeout, 5), unit);
        }

        return value;
    }

    /**
     * 获取或设置缓存（默认30分钟）
     */
    public <T> T getOrSet(String key, Supplier<T> loader) {
        return getOrSet(key, loader, 30, TimeUnit.MINUTES);
    }

    /**
     * 删除匹配模式的key
     */
    public void deletePattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (!CollectionUtils.isEmpty(keys)) {
            redisTemplate.delete(keys);
        }
    }

    // ====================== 类型转换 ======================

    /**
     * 将LinkedHashMap转换为对象
     */
    private <T> T convertMapToObject(LinkedHashMap<?, ?> map, Class<T> clazz) {
        try {
            // 使用注入的已配置好的 ObjectMapper
            return objectMapper.convertValue(map, clazz);
        } catch (Exception e) {
            log.error("Map转对象失败", e);
            return null;
        }
    }

    /**
     * 空值标记对象（用于防止缓存穿透）
     */
    private static class NullValue {
        private final boolean empty = true;
    }

    // ====================== 便捷方法 ======================

    /**
     * 设置对象缓存（30分钟过期）
     */
    public void setObject(String key, Object value) {
        set(key, value, 30, TimeUnit.MINUTES);
    }

    /**
     * 设置字符串缓存
     */
    public void setString(String key, String value, long timeout, TimeUnit unit) {
        set(key, value, timeout, unit);
    }

    /**
     * 获取字符串
     */
    public String getString(String key) {
        Object value = get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 设置列表
     */
    public <T> void setList(String key, List<T> list, long timeout, TimeUnit unit) {
        set(key, list, timeout, unit);
    }

    /**
     * 获取列表
     */
    public <T> List<T> getList(String key, Class<T> elementType) {
        Object value = get(key);
        if (value instanceof List) {
            return (List<T>) value;
        }
        return null;
    }

    /**
     * 使用SCAN命令查找所有以指定前缀为前缀的键
     * 相比keys命令，SCAN命令不会阻塞Redis服务，更适合生产环境
     *
     * @param prefix 键的前缀
     * @param count 每次迭代返回的元素数量（建议1000）
     * @return 匹配的键集合，如果没有匹配的键则返回空集合
     */
    public Set<String> scanKeysWithPrefix(String prefix, long count) {
        Set<String> keys = new HashSet<>();

        try {
            ScanOptions options = ScanOptions.scanOptions()
                    .match(prefix + "*")
                    .count(count)
                    .build();

            redisTemplate.executeWithStickyConnection(connection -> {
                try (var cursor = connection.scan(options)) {
                    cursor.forEachRemaining(key -> keys.add(new String(key)));
                    return null;
                } catch (Exception e) {
                    log.error("Redis SCAN操作失败", e);
                    return null;
                }
            });

        } catch (Exception e) {
            log.error("获取Redis键失败，前缀: {}", prefix, e);
        }

        return keys;
    }

    /**
     * 使用SCAN命令查找所有以指定前缀为前缀的键
     */
    public Set<String> scanKeysWithPrefix(String prefix) {
        return scanKeysWithPrefix(prefix, 1000);
    }

    /**
     * 删除所有以指定前缀为前缀的键
     *
     * @param prefix 键的前缀
     * @return 删除的键数量
     */
    public long deleteKeysWithPrefix(String prefix) {
        Set<String> keys = scanKeysWithPrefix(prefix);
        if (!keys.isEmpty()) {
            Long deleted = redisTemplate.delete(keys);
            return deleted != null ? deleted : 0;
        }
        return 0;
    }
}

