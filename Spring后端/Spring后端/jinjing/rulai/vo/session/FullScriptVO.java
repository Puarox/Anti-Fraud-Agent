package rulai.vo.session;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * <p>
 * 当前状态全部可用话术内容VO
 * </p>
 *
 * @author Mistry
 * @since 2025-12-30
 */

@Getter
@Setter
@ToString
public class FullScriptVO {

    /**
     * 当前所在节点的话术
     */
    List<ScriptVO> nodeScript;

    /**
     * 当前会话的反馈话术
     */
    List<ScriptVO> feedbackScript;

    /**
     * 通用话术
     */
    List<ScriptVO> commonScript;
}
