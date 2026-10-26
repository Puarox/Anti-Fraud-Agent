package rulai.httpstatus.exceptions;

/**
 * <p>
 * 405错误类
 * 客户端请求方法被禁止
 * </p>
 *
 * @author Mistry
 * @since 2025-06-23
 */

public class MethodNotAllowedException extends BaseHTTPException {
    public MethodNotAllowedException() {
        super();
    }

    public MethodNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }

    public MethodNotAllowedException(String message) {
        super(message);
    }

    public MethodNotAllowedException(Throwable cause) {
        super(cause);
    }
}
