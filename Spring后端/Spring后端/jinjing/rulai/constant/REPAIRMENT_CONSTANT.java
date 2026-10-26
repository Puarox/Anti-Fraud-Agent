package rulai.constant;

/**
 * <p>
 * 维修常量
 * </p>
 *
 * @author Mistry
 * @since 2025-01-08
 */

public final class REPAIRMENT_CONSTANT {
    public enum STATUS {
        PENDING, PROCESSING, FINISHED
    }

    public static final String DEFAULT_STATUS = STATUS.PENDING.name();
}