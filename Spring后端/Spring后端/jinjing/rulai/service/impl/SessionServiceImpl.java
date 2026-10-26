package rulai.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import rulai.constant.SESSION_CONSTANT;
import rulai.convertor.ISessionConverter;
import rulai.entity.Edge;
import rulai.entity.Node;
import rulai.pojo.Session;
import rulai.service.IEdgeService;
import rulai.service.INodeService;
import rulai.service.ISessionService;
import rulai.utils.AIUtils;
import rulai.utils.RedisUtils;
import rulai.vo.session.OutEdgeVO;
import rulai.vo.session.SessionStateVO;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 会话服务实现类
 * </p>
 *
 * @author Mistry
 * @since 2025-12-30
 */
@Service
public class SessionServiceImpl implements ISessionService {
    @Autowired
    RedisUtils redisUtils;

    @Autowired
    ISessionConverter sessionConverter;

    @Lazy
    @Autowired
    IEdgeService edgeService;

    @Lazy
    @Autowired
    INodeService nodeService;

    @Autowired
    private AIUtils aIUtils;

    @Override
    public Session getSession(String sessionId) {
        return redisUtils.get(SESSION_CONSTANT.REDIS_PREFIX + sessionId, Session.class);
    }
    @Override
    public void setSession(String sessionId, Session session) {
        redisUtils.set(SESSION_CONSTANT.REDIS_PREFIX + session.getId(), session);
    }
    @Override
    public void deleteSession(String sessionId) {
        redisUtils.delete(SESSION_CONSTANT.REDIS_PREFIX + sessionId);
    }

    @Override
    public SessionStateVO getStateBySession(Session session) {
        List<Edge> edgeList = edgeService.getOutEdges(session.getCurrentNodeId());
        List<OutEdgeVO> outEdgeList = new ArrayList<>();
        for (Edge edge : edgeList) {
            OutEdgeVO outEdgeVO = new OutEdgeVO();

            outEdgeVO.setEdgeId(edge.getId());
            outEdgeVO.setEdgeName(edge.getName());
            outEdgeVO.setEdgeType(edge.getType());
            outEdgeVO.setEdgeDescription(edge.getDescription());

            Node node = nodeService.getById(edge.getTargetId());
            outEdgeVO.setNodeId(node.getId());
            outEdgeVO.setNodeName(node.getName());
            outEdgeVO.setNodeType(node.getType());
            outEdgeVO.setNodeDescription(node.getDescription());

            outEdgeList.add(outEdgeVO);
        }

        SessionStateVO sessionStateVO = sessionConverter.toSessionStateVO(session);
        sessionStateVO.setOutEdge(outEdgeList);

        aIUtils.addScore(sessionStateVO);

        if(!session.getUndoQue().isEmpty()){
            sessionStateVO.setUndoNodeId(session.getUndoQue().getFirst());
        }

        return sessionStateVO;
    }

    /**
     * 我并没有实现锁，所以不要用这个方法
     */
    @Async("logExecutor")
    @Override
    public void addOperationAsync(String sessionId, String logId) {
        Session session = getSession(sessionId);
        session.getOperations().add(logId);
        setSession(sessionId, session);
    }

    @Override
    public void addOperation(String sessionId, String logId) {
        Session session = getSession(sessionId);
        session.getOperations().add(logId);
        setSession(sessionId, session);
    }
}
