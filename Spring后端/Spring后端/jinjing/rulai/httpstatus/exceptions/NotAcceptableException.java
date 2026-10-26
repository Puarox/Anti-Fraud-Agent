package rulai.httpstatus.exceptions;

/**
 * <p>
 * 406错误类
 * 服务器无法根据客户端的请求找到资源
 * </p>
 *
 * @author Mistry
 * @since 2025-06-23
 */

public class NotAcceptableException extends BaseHTTPException {
    public NotAcceptableException() {
        super();
    }

    public NotAcceptableException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotAcceptableException(String message) {
        super(message);
    }

    public NotAcceptableException(Throwable cause) {
        super(cause);
    }
}
