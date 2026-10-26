package rulai.constant;

/**
 * <p>
 * 边常量
 * </p>
 *
 * @author Mistry
 * @since 2025-12-29
 */

public final class NODE_CONSTANT {
    public enum TYPE {
        CREATE, READ, DELETE, UPDATE, START, END
    }

    public static final String DEFAULT_NAME = "未命名节点";
    public static final String DEFAULT_TYPE = TYPE.START.name();
}