package rulai.httpstatus.exceptions;

/**
 * <p>
 * 502错误类
 * 作为网关或者代理工作的服务器尝试执行请求时，从远程服务器接收到了一个无效的响应
 * </p>
 *
 * @author Mistry
 * @since 2025-06-23
 */

public class BadGatewayException extends BaseHTTPException {
    public BadGatewayException() {
        super();
    }

    public BadGatewayException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadGatewayException(String message) {
        super(message);
    }

    public BadGatewayException(Throwable cause) {
        super(cause);
    }
}
