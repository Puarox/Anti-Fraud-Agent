package rulai.httpstatus.exceptions;

/**
 * <p>
 * 400错误类
 * 客户端请求的语法错误，服务器无法理解
 * </p>
 *
 * @author Mistry
 * @since 2025-06-23
 */

public class BadRequestException extends BaseHTTPException {
    public BadRequestException() {
        super();
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(Throwable cause) {
        super(cause);
    }
}
