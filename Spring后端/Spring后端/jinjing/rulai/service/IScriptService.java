package rulai.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import rulai.constant.SCRIPT_CONSTANT;
import rulai.dto.ScriptCreateDTO;
import rulai.dto.ScriptModifyDTO;
import rulai.entity.Script;
import com.baomidou.mybatisplus.extension.service.IService;
import rulai.pojo.Dialogue;
import rulai.pojo.Session;
import rulai.vo.graph.ScriptManageVO;
import rulai.vo.session.FullScriptVO;
import rulai.vo.session.SessionStateVO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 话术内容表 服务类
 * </p>
 *
 * @author Mistry
 * @since 2025-12-30
 */
public interface IScriptService extends IService<Script> {
    public Script createScript(ScriptCreateDTO dto);
    public void updateScript(ScriptModifyDTO dto);
    public ScriptManageVO getScript(String scriptId);
    public void deleteScript(String scriptId);
    public FullScriptVO getScriptBySession(Session session);
    public List<Script> getNodeScript(String nodeId);
    public List<ScriptManageVO> listCommonScript();
    public List<ScriptManageVO> listFeedbackScript();
    public List<ScriptManageVO> listNodeScriptByNodeId(String nodeId);

    public List<Dialogue> getContextBySession(Session session);
//    public Script getEdgeEnabledScript(String nodeId);
//    public Script getEdgeDisabledScript(String nodeId);
}
