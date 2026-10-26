package rulai.constant;

/**
 * <p>
 * 边常量
 * </p>
 *
 * @author Mistry
 * @since 2025-12-29
 */

public final class EDGE_CONSTANT {
    public enum TYPE {
        CONDITIONAL,
        UNCONDITIONAL
    }

    public static final String DEFAULT_NAME = "未命名边";
    public static final String DEFAULT_TYPE = TYPE.CONDITIONAL.name();
}
