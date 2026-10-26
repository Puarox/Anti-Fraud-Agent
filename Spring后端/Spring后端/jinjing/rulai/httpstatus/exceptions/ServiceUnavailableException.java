package rulai.httpstatus.exceptions;

/**
 * <p>
 * 503错误类
 * 由于超载或系统维护，服务器暂时的无法处理客户端的请求。
 * </p>
 *
 * @author Mistry
 * @since 2025-06-23
 */

public class ServiceUnavailableException extends BaseHTTPException {
    public ServiceUnavailableException() {
        super();
    }

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceUnavailableException(String message) {
        super(message);
    }

    public ServiceUnavailableException(Throwable cause) {
        super(cause);
    }
}
