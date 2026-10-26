package rulai.service;

import org.springframework.web.bind.support.SessionStatus;
import rulai.pojo.Session;
import rulai.vo.session.SessionStateVO;

/**
 * <p>
 * 会话服务类
 * </p>
 *
 * @author Mistry
 * @since 2025-12-30
 */
public interface ISessionService {
    public Session getSession(String sessionId);
    public void setSession(String sessionId, Session session);
    public void deleteSession(String sessionId);
    SessionStateVO getStateBySession(Session session);
    public void addOperationAsync(String sessionId, String logId);
    public void addOperation(String sessionId, String logId);
}
