package rulai.constant;

/**
 * <p>
 * 套餐常量
 * </p>
 *
 * @author Mistry
 * @since 2025-01-08
 */

public final class BUNDLE_CONSTANT {
    public enum STATUS {
        ACTIVE, SUSPENDED, TERMINATED
    }

    public static final String DEFAULT_STATUS = STATUS.SUSPENDED.name();
}