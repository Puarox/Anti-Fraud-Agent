package rulai.service;

/**
 * <p>
 * 会话服务类
 * </p>
 *
 * @author Mistry
 * @since 2025-12-30
 */
public interface IInterfaceService {
    Object call(String interfaceName, Object ...arg);
    public String resolve(String template, Object ...arg);
    String check(String template);
}
