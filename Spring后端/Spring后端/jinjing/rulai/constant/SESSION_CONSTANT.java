package rulai.constant;

/**
 * <p>
 * 边常量
 * </p>
 *
 * @author Mistry
 * @since 2025-12-29
 */

public final class SESSION_CONSTANT {

    public enum STATE{
        IN_PROGRESS, PAUSED, ENDED
    }

    public static final String REDIS_PREFIX = "session:";
    public static final String DEFAULT_STATE = STATE.IN_PROGRESS.name();
}