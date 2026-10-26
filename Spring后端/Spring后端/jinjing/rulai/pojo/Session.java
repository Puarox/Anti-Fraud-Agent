package rulai.pojo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import rulai.constant.FOOTPRINT_CONSTANT;
import rulai.entity.Edge;
import rulai.entity.Node;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * <p>
 * 服务状态维护实体
 * </p>
 *
 * @author Mistry
 * @since 2025-12-30
 */

@Getter
@Setter
@ToString
public class Session {
    /**
     * 会话ID
     */
    private String id;

    /**
     * 会话开始时间
     */
    private LocalDateTime startTime;

    /**
     * 会话状态
     */
    private String status;

    /**
     * 客户ID
     */
    private String customerId;

    /**
     * 客户ID
     */
    private String userId;

    /**
     * 当前状态ID
     */
    private String currentNodeId;

    /**
     * 支持的撤销队列
     */
    private Deque<String> undoQue = new ArrayDeque<>();

    /**
     * 支持的重做队列
     */
    private Deque<String> redoQue = new ArrayDeque<>();

    /**
     * 服务哼唧
     */
    private List<Footprint> trace = new ArrayList<>();

    /**
     * 对话上下文
     */
    private List<Dialogue> context = new ArrayList<>();

    /**
     * 客服操作日志Id
     */
    private List<String> operations = new ArrayList<>();

    public void start(Node node) {
        currentNodeId = node.getId();
        step(null, node.getId(), FOOTPRINT_CONSTANT.TYPE.START.name());
    }

    public void end(Node node) {
        currentNodeId = node.getId();
        step(null, node.getId(), FOOTPRINT_CONSTANT.TYPE.END.name());
    }

    public void forward(Edge edge, Node node) {
        undoQue.addFirst(currentNodeId);
        currentNodeId = node.getId();
        redoQue.clear();
        step(edge.getId(), node.getId(), FOOTPRINT_CONSTANT.TYPE.FORWARD.name());
    }

    public void redo() {
        if(!redoQue.isEmpty()) {
            String nodeId = redoQue.getLast();
            undoQue.addFirst(nodeId);
            currentNodeId = nodeId;
            redoQue.removeLast();
            step(null, nodeId,  FOOTPRINT_CONSTANT.TYPE.REDO.name());
        } else {
            throw new IndexOutOfBoundsException("无法重做，重做队列空");
        }
    }

    public void undo() {
        if(!undoQue.isEmpty()) {
            String nodeId = undoQue.getFirst();
            redoQue.addLast(nodeId);
            currentNodeId = nodeId;
            undoQue.removeFirst();
            step(null, nodeId,  FOOTPRINT_CONSTANT.TYPE.UNDO.name());
        } else {
            throw new IndexOutOfBoundsException("无法撤销，撤销队列空");
        }
    }

    public void step(String edgeId, String nodeId, String type) {
        Footprint footprint = new Footprint();
        footprint.setNodeId(nodeId);
        footprint.setEdgeId(edgeId);
        footprint.setType(type);
        footprint.setTime(LocalDateTime.now());
        trace.add(footprint);
    }

    public void dialogue(Dialogue dialogue) {
        context.add(dialogue);
    }

    public void operate(String operationId) {
        operations.add(operationId);
    }
}
