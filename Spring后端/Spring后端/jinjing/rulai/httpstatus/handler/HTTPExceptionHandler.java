package rulai.httpstatus.handler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import rulai.common.APIResponse;
import rulai.httpstatus.exceptions.*;
import rulai.weblogger.WebLog;

import java.util.Arrays;

/**
 * <p>
 * 状态码处理类
 * </p>
 *
 * @author Mistry
 * @since 2025-06-23
 */

@ControllerAdvice
public class HTTPExceptionHandler {

    /**
     * 400
     */
    @ResponseBody
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @WebLog
    public APIResponse handle(BadRequestException e) {
        return APIResponse.error(e.getMessage());
    }

    /**
     * 401
     */
    @ResponseBody
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    @WebLog
    public APIResponse handle(UnauthorizedException e) {
        return APIResponse.error(e.getMessage());
    }

    /**
     * 402
     */
    @ResponseBody
    @ExceptionHandler(PaymentRequiredException.class)
    @ResponseStatus(value = HttpStatus.PAYMENT_REQUIRED)
    @WebLog
    public APIResponse handle(PaymentRequiredException e) {
        return APIResponse.error(e.getMessage());
    }

    /**
     * 403
     */
    @ResponseBody
    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    @WebLog
    public APIResponse handle(ForbiddenException e) {
        return APIResponse.error(e.getMessage());
    }

    /**
     * 404
     */
    @ResponseBody
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @WebLog
    public APIResponse handle(ResourceNotFoundException e) {
        return APIResponse.error(e.getMessage());
    }

    /**
     * 405
     */
    @ResponseBody
    @ExceptionHandler(MethodNotAllowedException.class)
    @ResponseStatus(value = HttpStatus.METHOD_NOT_ALLOWED)
    @WebLog
    public APIResponse handle(MethodNotAllowedException e) {
        return APIResponse.error(e.getMessage());
    }

    /**
     * 406
     */
    @ResponseBody
    @ExceptionHandler(NotAcceptableException.class)
    @ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
    @WebLog
    public APIResponse handle(NotAcceptableException e) {
        return APIResponse.error(e.getMessage());
    }

    /**
     * 407
     */
    @ResponseBody
    @ExceptionHandler(ProxyAuthenticationRequiredException.class)
    @ResponseStatus(value = HttpStatus.PROXY_AUTHENTICATION_REQUIRED)
    @WebLog
    public APIResponse handle(ProxyAuthenticationRequiredException e) {
        return APIResponse.error(e.getMessage());
    }

    /**
     * 408
     */
    @ResponseBody
    @ExceptionHandler(RequestTimeOutException.class)
    @ResponseStatus(value = HttpStatus.REQUEST_TIMEOUT)
    @WebLog
    public APIResponse handle(RequestTimeOutException e) {
        return APIResponse.error(e.getMessage());
    }

    /**
     * 409
     */
    @ResponseBody
    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    @WebLog
    public APIResponse handle(ConflictException e) {
        return APIResponse.error(e.getMessage());
    }

    /**
     * 410
     */
    @ResponseBody
    @ExceptionHandler(GoneException.class)
    @ResponseStatus(value = HttpStatus.GONE)
    @WebLog
    public APIResponse handle(GoneException e) {
        return APIResponse.error(e.getMessage());
    }

    /**
     * 500
     */
    @ResponseBody
    @ExceptionHandler(InternalServeErrorException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @WebLog
    public APIResponse handle(InternalServeErrorException e) {
        return APIResponse.error(e.getMessage());
    }

    /**
     * 501
     */
    @ResponseBody
    @ExceptionHandler(NotImplementedException.class)
    @ResponseStatus(value = HttpStatus.NOT_IMPLEMENTED)
    @WebLog
    public APIResponse handle(NotImplementedException e) {
        return APIResponse.error(e.getMessage());
    }

    /**
     * 502
     */
    @ResponseBody
    @ExceptionHandler(BadGatewayException.class)
    @ResponseStatus(value = HttpStatus.BAD_GATEWAY)
    @WebLog
    public APIResponse handle(BadGatewayException e) {
        return APIResponse.error(e.getMessage());
    }

    /**
     * 503
     */
    @ResponseBody
    @ExceptionHandler(ServiceUnavailableException.class)
    @ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
    @WebLog
    public APIResponse handle(ServiceUnavailableException e) {
        return APIResponse.error(e.getMessage());
    }

    /**
     * 504
     */
    @ResponseBody
    @ExceptionHandler(GatewayTimeOutException.class)
    @ResponseStatus(value = HttpStatus.GATEWAY_TIMEOUT)
    @WebLog
    public APIResponse handle(GatewayTimeOutException e) {
        return APIResponse.error(e.getMessage());
    }

    /**
     * 505
     */
    @ResponseBody
    @ExceptionHandler(HTTPVisionNotSupportedException.class)
    @ResponseStatus(value = HttpStatus.HTTP_VERSION_NOT_SUPPORTED)
    @WebLog
    public APIResponse handle(HTTPVisionNotSupportedException e) {
        return APIResponse.error(e.getMessage());
    }

    /**
     * 捕获校验失败
     */
    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @WebLog
    public APIResponse handle(MethodArgumentNotValidException e) {
        return APIResponse.error(Arrays.toString(e.getDetailMessageArguments()));
    }
}
