package rulai.httpstatus.exceptions;

/**
 * <p>
 * 407错误类
 * 请求要求代理的身份认证，与 401 类似，但请求者应当使用代理进行授权
 * </p>
 *
 * @author Mistry
 * @since 2025-06-23
 */

public class ProxyAuthenticationRequiredException extends BaseHTTPException {
    public ProxyAuthenticationRequiredException() {
        super();
    }

    public ProxyAuthenticationRequiredException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProxyAuthenticationRequiredException(String message) {
        super(message);
    }

    public ProxyAuthenticationRequiredException(Throwable cause) {
        super(cause);
    }
}
