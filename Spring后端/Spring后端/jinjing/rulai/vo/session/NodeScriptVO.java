package rulai.vo.session;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <p>
 * 节点话术内容VO
 * </p>
 *
 * @author Mistry
 * @since 2025-12-30
 */

@Getter
@Setter
@ToString
public class NodeScriptVO {

    /**
     * 节点唯一ID
     */
    private String id;

    /**
     * 节点名
     */
    private String name;

    /**
     * 节点类型
     */
    private String operations;

    /**
     * ScriptVO 自身
     */
    private ScriptVO script;
}
