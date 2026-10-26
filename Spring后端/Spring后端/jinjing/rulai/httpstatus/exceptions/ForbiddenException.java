package rulai.httpstatus.exceptions;

/**
 * <p>
 * 403错误类
 * 服务器理解请求，但是拒绝
 * </p>
 *
 * @author Mistry
 * @since 2025-06-23
 */

public class ForbiddenException extends BaseHTTPException {
    public ForbiddenException() {
        super();
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(Throwable cause) {
        super(cause);
    }
}
