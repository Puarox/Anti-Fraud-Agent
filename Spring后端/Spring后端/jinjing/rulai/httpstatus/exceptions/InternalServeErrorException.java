package rulai.httpstatus.exceptions;

/**
 * <p>
 * 500错误类
 * 服务器内部错误，无法完成请求
 * </p>
 *
 * @author Mistry
 * @since 2025-06-23
 */

public class InternalServeErrorException extends BaseHTTPException {
    public InternalServeErrorException() {
        super();
    }

    public InternalServeErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public InternalServeErrorException(String message) {
        super(message);
    }

    public InternalServeErrorException(Throwable cause) {
        super(cause);
    }
}
