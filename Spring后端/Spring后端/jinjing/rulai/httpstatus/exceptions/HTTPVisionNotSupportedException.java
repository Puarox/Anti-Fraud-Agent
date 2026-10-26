package rulai.httpstatus.exceptions;

/**
 * <p>
 * 505错误类
 * 服务器不支持请求的HTTP协议的版本，无法完成处理
 * </p>
 *
 * @author Mistry
 * @since 2025-06-23
 */

public class HTTPVisionNotSupportedException extends BaseHTTPException {
    public HTTPVisionNotSupportedException() {
        super();
    }

    public HTTPVisionNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }

    public HTTPVisionNotSupportedException(String message) {
        super(message);
    }

    public HTTPVisionNotSupportedException(Throwable cause) {
        super(cause);
    }
}
