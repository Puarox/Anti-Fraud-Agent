package rulai.httpstatus.exceptions;

/**
 * <p>
 * 504错误类
 * 充当网关或代理的服务器，未及时从远端服务器获取请求
 * </p>
 *
 * @author Mistry
 * @since 2025-06-23
 */

public class GatewayTimeOutException extends BaseHTTPException {
    public GatewayTimeOutException() {
        super();
    }

    public GatewayTimeOutException(String message, Throwable cause) {
        super(message, cause);
    }

    public GatewayTimeOutException(String message) {
        super(message);
    }

    public GatewayTimeOutException(Throwable cause) {
        super(cause);
    }
}
