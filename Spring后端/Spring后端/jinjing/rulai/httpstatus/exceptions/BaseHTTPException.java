package rulai.httpstatus.exceptions;

/**
 * <p>
 * 基础的HTTP异常类
 * </p>
 *
 * @author Mistry
 * @since 2025-06-23
 */

public class BaseHTTPException extends RuntimeException {
    public BaseHTTPException() {
        super();
    }

    public BaseHTTPException(String message, Throwable cause) {
        super(message, cause);
    }

    public BaseHTTPException(String message) {
        super(message);
    }

    public BaseHTTPException(Throwable cause) {
        super(cause);
    }
}
