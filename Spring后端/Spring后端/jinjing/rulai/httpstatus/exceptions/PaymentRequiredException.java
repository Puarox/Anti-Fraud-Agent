package rulai.httpstatus.exceptions;

/**
 * <p>
 * 402错误类
 * 保留将来使用
 * </p>
 *
 * @author Mistry
 * @since 2025-06-23
 */

public class PaymentRequiredException extends BaseHTTPException {
    public PaymentRequiredException() {
        super();
    }

    public PaymentRequiredException(String message, Throwable cause) {
        super(message, cause);
    }

    public PaymentRequiredException(String message) {
        super(message);
    }

    public PaymentRequiredException(Throwable cause) {
        super(cause);
    }
}
