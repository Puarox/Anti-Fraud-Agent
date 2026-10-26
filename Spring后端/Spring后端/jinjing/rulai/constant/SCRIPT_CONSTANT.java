package rulai.constant;

/**
 * <p>
 * 边常量
 * </p>
 *
 * @author Mistry
 * @since 2025-12-29
 */

public final class SCRIPT_CONSTANT {

    public enum TYPE {
        NODE, COMMON, FEEDBACK
    }

    public static final String DEFAULT_NAME = "未命名话术";
    public static final String DEFAULT_SCRIPT = "您好，这是一段样例话术！";
    public static final String DEFAULT_TYPE = TYPE.NODE.name();

    private final String value;

    SCRIPT_CONSTANT(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}