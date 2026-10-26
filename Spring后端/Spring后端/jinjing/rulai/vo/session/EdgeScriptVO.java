package rulai.vo.session;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <p>
 * 边话术内容VO
 * </p>
 *
 * @author Mistry
 * @since 2025-12-30
 */

@Getter
@Setter
@ToString
public class EdgeScriptVO {

    /**
     * 边唯一ID
     */
    private String id;

    /**
     * 边名
     */
    private String name;

    /**
     * 有向边终点id
     */
    private String targetId;

    /**
     * 这条边是否可以转移
     */
    private Boolean enabled;

    /**
     * ScriptVO 自身
     */
    private ScriptVO script;
}
