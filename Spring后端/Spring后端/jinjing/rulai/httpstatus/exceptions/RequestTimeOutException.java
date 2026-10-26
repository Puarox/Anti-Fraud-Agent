package rulai.httpstatus.exceptions;

/**
 * <p>
 * 408错误类
 * 超时
 * </p>
 *
 * @author Mistry
 * @since 2025-06-23
 */

public class RequestTimeOutException extends BaseHTTPException {
    public RequestTimeOutException() {
        super();
    }

    public RequestTimeOutException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequestTimeOutException(String message) {
        super(message);
    }

    public RequestTimeOutException(Throwable cause) {
        super(cause);
    }
}
