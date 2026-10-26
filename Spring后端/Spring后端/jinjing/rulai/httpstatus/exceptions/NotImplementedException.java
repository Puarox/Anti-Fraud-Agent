package rulai.httpstatus.exceptions;

/**
 * <p>
 * 501错误类
 * 服务器不支持请求的功能，无法完成请求
 * </p>
 *
 * @author Mistry
 * @since 2025-06-23
 */

public class NotImplementedException extends BaseHTTPException {
    public NotImplementedException() {
        super();
    }

    public NotImplementedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotImplementedException(String message) {
        super(message);
    }

    public NotImplementedException(Throwable cause) {
        super(cause);
    }
}
