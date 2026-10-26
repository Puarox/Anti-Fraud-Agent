package rulai.vo.session;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import rulai.vo.graph.EdgeVO;

import java.util.List;

/**
 * <p>
 * 出边VO
 * </p>
 *
 * @author Mistry
 * @since 2025-01-06
 */
@Getter
@Setter
@ToString
public class OutEdgeVO {

    /**
     * 边唯一ID
     */
    private String edgeId;

    /**
     * 边类型
     */
    private String edgeType;

    /**
     * 边名
     */
    private String edgeName;

    /**
     * 边描述
     */
    private String edgeDescription;

    /**
     * 出边指向节点
     */
    private String nodeId;

    /**
     * 节点名
     */
    private String nodeName;

    /**
     * 节点类型
     */
    private String nodeType;

    /**
     * 节点描述
     */
    private String nodeDescription;

    /**
     * 走这条边的置信度
     */
    private Float score;
}
