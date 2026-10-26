package rulai.httpstatus.exceptions;

/**
 * <p>
 * 404错误类
 * 服务器无法根据客户端的请求找到资源。
 * </p>
 *
 * @author Mistry
 * @since 2025-06-23
 */

public class ResourceNotFoundException extends BaseHTTPException {
    public ResourceNotFoundException() {
        super();
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(Throwable cause) {
        super(cause);
    }
}
