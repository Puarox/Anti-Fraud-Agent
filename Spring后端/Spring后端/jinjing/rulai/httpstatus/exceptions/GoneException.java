package rulai.httpstatus.exceptions;

/**
 * <p>
 * 410错误类
 * 客户端请求的资源不存在，410不同于404，如果资源以前有现在被永久删除了可使用410代码，网站设计人员可通过301代码指定资源的新位置
 * </p>
 *
 * @author Mistry
 * @since 2025-06-23
 */

public class GoneException extends BaseHTTPException {
    public GoneException() {
        super();
    }

    public GoneException(String message, Throwable cause) {
        super(message, cause);
    }

    public GoneException(String message) {
        super(message);
    }

    public GoneException(Throwable cause) {
        super(cause);
    }
}
