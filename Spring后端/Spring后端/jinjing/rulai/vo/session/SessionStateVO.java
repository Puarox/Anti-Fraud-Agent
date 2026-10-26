package rulai.vo.session;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import rulai.pojo.Dialogue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 会话状态VO
 * </p>
 *
 * @author Mistry
 * @since 2025-01-06
 */
@Getter
@Setter
@ToString
public class SessionStateVO {
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
     * 上一步节点ID
     */
    private String undoNodeId;

    /**
     * 对话上下文
     */
    private List<Dialogue> context = new ArrayList<>();

    /**
     * 当前状态出边
     */
    private List<OutEdgeVO> outEdge = new ArrayList<>();
}
