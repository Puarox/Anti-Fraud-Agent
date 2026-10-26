package rulai.httpstatus.exceptions;

/**
 * <p>
 * 401错误类
 * 请求要求用户的身份认证
 * </p>
 *
 * @author Mistry
 * @since 2025-06-23
 */

public class UnauthorizedException extends BaseHTTPException {
    public UnauthorizedException() {
        super();
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(Throwable cause) {
        super(cause);
    }
}
