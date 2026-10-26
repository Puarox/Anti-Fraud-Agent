package rulai.constant;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mistry
 */
public final class USER_CONSTANT {

    static {
        Map<Integer, String> tempMap = new HashMap<>();
        tempMap.put(1<<0, "USER");
        tempMap.put(1<<1, "ADMIN");
        tempMap.put(1<<2, "SUPER_ADMIN");

        Map<Integer, String> PERMISSION_MAP = Collections.unmodifiableMap(tempMap);
    }


    public static final String REDIS_JWT_BLACKLIST_PREFIX ="jwt:blacklist:";

    public enum GENDER_ENUM
    {
        F,M,A
    }

    public enum STATUS
    {
        AVAILABLE,BUSY,POST_PROCESS,BREAK,OFFLINE,OTHER
    }
}
