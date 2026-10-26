package rulai.httpstatus.exceptions;

/**
 * <p>
 * 409错误类
 * PUT 时发生冲突
 * </p>
 *
 * @author Mistry
 * @since 2025-06-23
 */

public class ConflictException extends BaseHTTPException {
    public ConflictException() {
        super();
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(Throwable cause) {
        super(cause);
    }
}
