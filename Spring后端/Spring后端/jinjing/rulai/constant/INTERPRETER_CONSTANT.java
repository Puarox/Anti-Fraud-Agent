package rulai.constant;

/**
 * <p>
 * 解释器常量
 * </p>
 *
 * @author Mistry
 * @since 2025-12-29
 */

public final class INTERPRETER_CONSTANT {

    public enum OPERATION {
        EQ, NE, GT, GE, LT, LE, IN, CONTAINS, STARTSWITH, ENDSWITH
    }

    public enum EXPR_TYPE {
        ATOMIC, AND, OR, NOT
    }
}