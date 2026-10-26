package rulai.utils;

/*
  JWT工具类
 */
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import rulai.httpstatus.exceptions.UnauthorizedException;


import java.util.Calendar;
import java.util.Map;

import static rulai.constant.USER_CONSTANT.REDIS_JWT_BLACKLIST_PREFIX;

@Component
public class JwtUtils {

    /**
     * 生成token  header.payload.singature
     */
    private static final String SING = "RLFZ";

    private static RedisTemplate<String, Object> staticRedisTemplate;

    @Qualifier("redisTemplate")
    @Autowired
    private RedisTemplate<String, Object> instanceRedisTemplate;

    @PostConstruct
    public void init() {
        staticRedisTemplate = this.instanceRedisTemplate;
    }

    public static String getToken(Map<String, String> map) {

        Calendar instance = Calendar.getInstance();
        // 1天
        instance.add(Calendar.DATE, 1);

        JWTCreator.Builder builder = JWT.create();


        map.forEach(builder::withClaim);

        return builder.withExpiresAt(instance.getTime())
                .sign(Algorithm.HMAC256(SING));
    }

    /**
     * 验证token  合法性
     */
    public static DecodedJWT verify(String token) {
        String blacklistKey = REDIS_JWT_BLACKLIST_PREFIX + token;
        Boolean isLoggedOut = staticRedisTemplate.hasKey(blacklistKey) ;
        if(Boolean.TRUE.equals(isLoggedOut)){
            throw new UnauthorizedException("未登录，请登录后继续操作");
        }
        return JWT.require(Algorithm.HMAC256(SING)).build().verify(token);
    }

    /**
     * 获取token信息方法
     */
    public static DecodedJWT getTokenInfo(String token){
        return JWT.require(Algorithm.HMAC256(SING)).build().verify(token);
    }

    public static String getUsername(String token){
        DecodedJWT jwt = getTokenInfo(token);
        return jwt.getClaim("username").asString();
    }

    /**
     * 根据token取得用户ID
     * @param token
     * @return
     */
    public static String getId(String token)
    {
        DecodedJWT jwt = getTokenInfo(token);
        return jwt.getClaim("id").asString();

    }
}
