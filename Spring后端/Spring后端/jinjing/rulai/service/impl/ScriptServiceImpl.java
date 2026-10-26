package rulai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import rulai.constant.SCRIPT_CONSTANT;
import rulai.convertor.IScriptConverter;
import rulai.dto.ScriptCreateDTO;
import rulai.dto.ScriptModifyDTO;
import rulai.entity.Edge;
import rulai.entity.OperationLog;
import rulai.entity.Script;
import rulai.httpstatus.exceptions.ResourceNotFoundException;
import rulai.mapper.ScriptMapper;
import rulai.pojo.Dialogue;
import rulai.pojo.Session;
import rulai.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import rulai.utils.AIUtils;
import rulai.utils.interpolation.ConditionInterpreter;
import rulai.vo.graph.ScriptManageVO;
import rulai.vo.session.FullScriptVO;
import rulai.vo.session.ScriptVO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * <p>
 * 话术内容表 服务实现类
 * </p>
 *
 * @author Mistry
 * @since 2025-12-30
 */
@Service
public class ScriptServiceImpl extends ServiceImpl<ScriptMapper, Script> implements IScriptService {
    @Autowired
    IScriptConverter scriptConverter;
    
    @Autowired
    IEdgeService scriptService;

    @Autowired
    IInterfaceService interpreterService;
    @Autowired
    private ConditionInterpreter conditionInterpreter;

    @Autowired
    ISessionService sessionService;

    @Autowired
    IOperationLogService operationLogService;

    @Autowired
    private AIUtils aIUtils;

    @Override
    public Script createScript(ScriptCreateDTO dto) {
        Script script = scriptConverter.fromDTO(dto);
        System.out.println("script = " + script);
//        if(script.getType().equals(SCRIPT_CONSTANT.TYPE.NODE.name()) &&
//                getNodeScript(script.getAssociateId()) != null){
//            throw new BadRequestException("一个节点最多拥有一套话术");
//        }

        script.setId(UUID.randomUUID().toString());
        if(save(script)) {
            return getById(script.getId());
        } else {
            throw new ResourceNotFoundException("新建失败");
        }
    }
    

    @Override
    public void updateScript(ScriptModifyDTO dto) {
        Script script = scriptConverter.fromDTO(dto);
        if(updateById(script)) {
        } else {
            throw new ResourceNotFoundException("目标话术不存在");
        }
    }

    @Override
    public ScriptManageVO getScript(String scriptId) {
        Script script = getById(scriptId);
        if (script == null) {
            throw new ResourceNotFoundException("话术不存在");
        }
        ScriptManageVO scriptManageVO = scriptConverter.toScriptManageVO(script);
        return scriptManageVO;
    }

    @Transactional
    @Override
    public void deleteScript(String scriptId) {
        Script script = getById(scriptId);

        scriptService.batchDelete(scriptService.getInEdges(scriptId).stream().map(Edge::getId).collect(Collectors.toList()));
        scriptService.batchDelete(scriptService.getOutEdges(scriptId).stream().map(Edge::getId).collect(Collectors.toList()));
        removeById(scriptId);
    }

    @Override
    public List<Script> getNodeScript(String nodeId) {
        LambdaQueryWrapper<Script> nodeWrapper = new LambdaQueryWrapper<>();
        nodeWrapper.eq(Script::getAssociateId, nodeId);
        return list(nodeWrapper);
    }

//    @Override
//    public Script getEdgeEnabledScript(String nodeId) {
//        LambdaQueryWrapper<Script> nodeWrapper = new LambdaQueryWrapper<>();
//        nodeWrapper.eq(Script::getAssociateId, nodeId).eq(Script::getType, SCRIPT_CONSTANT.TYPE_EDGE_ENABLED.getValue());
//        return getOne(nodeWrapper);
//    }
//
//    @Override
//    public Script getEdgeDisabledScript(String nodeId) {
//        LambdaQueryWrapper<Script> nodeWrapper = new LambdaQueryWrapper<>();
//        nodeWrapper.eq(Script::getAssociateId, nodeId).eq(Script::getType, SCRIPT_CONSTANT.TYPE_EDGE_ENABLED.getValue());
//        return getOne(nodeWrapper);
//    }

    @Override
    public List<ScriptManageVO> listCommonScript() {
        QueryWrapper<Script> commonWrapper = new QueryWrapper<>();
        commonWrapper.eq("type", SCRIPT_CONSTANT.TYPE.COMMON.name());
        List<ScriptManageVO> commonScript = list(commonWrapper)
                .stream()
                .map(scriptConverter::toScriptManageVO)
                .collect(Collectors.toList());
        return commonScript;
    }

    @Override
    public List<ScriptManageVO> listFeedbackScript() {
        QueryWrapper<Script> commonWrapper = new QueryWrapper<>();
        commonWrapper.eq("type", SCRIPT_CONSTANT.TYPE.FEEDBACK.name());
        List<ScriptManageVO> commonScript = list(commonWrapper)
                .stream()
                .map(scriptConverter::toScriptManageVO)
                .collect(Collectors.toList());
        return commonScript;
    }

    @Override
    public List<ScriptManageVO> listNodeScriptByNodeId(String nodeId) {
        QueryWrapper<Script> commonWrapper = new QueryWrapper<>();
        commonWrapper.eq("type", SCRIPT_CONSTANT.TYPE.NODE.name()).eq("associate_id", nodeId);
        List<ScriptManageVO> commonScript = list(commonWrapper)
                .stream()
                .map(scriptConverter::toScriptManageVO)
                .collect(Collectors.toList());
        return commonScript;
    }

    @Override
    public List<Dialogue> getContextBySession(Session session) {
        List<Dialogue> context = session.getContext();
        return context;
    }

    public ScriptVO resolve(Script script, Session session) {
        ScriptVO scriptVO = scriptConverter.toScriptVO(script);
        System.out.println("session = " + session);
        scriptVO.setSummary(interpreterService.resolve(script.getSummary(), session));
        scriptVO.setContent(interpreterService.resolve(script.getContent(), session));
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            if(script.getConditions().isEmpty()) {
                scriptVO.setEnabled(true);
            } else {
                List<Map<String, Object>> conditions = objectMapper.readValue(script.getConditions(), List.class);
                scriptVO.setEnabled(conditionInterpreter.evaluate(conditions, session));
            }
        } catch (Exception e) {
            e.printStackTrace();
            scriptVO.setEnabled(null);
        }
        return scriptVO;
    }

    public ScriptVO resolve(Script script, Session session, OperationLog operationLog) {
        ScriptVO scriptVO = scriptConverter.toScriptVO(script);
        scriptVO.setSummary(interpreterService.resolve(script.getSummary(), session, operationLog));
        scriptVO.setContent(interpreterService.resolve(script.getContent(), session, operationLog));
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            if(script.getConditions().isEmpty()) {
                scriptVO.setEnabled(true);
            } else {
                List<Map<String, Object>> conditions = objectMapper.readValue(script.getConditions(), List.class);
                scriptVO.setEnabled(conditionInterpreter.evaluate(conditions, session, operationLog));
            }
        } catch (Exception e) {
            scriptVO.setEnabled(null);
        }
        return scriptVO;
    }

    @Override
    public FullScriptVO getScriptBySession(Session session) {
        QueryWrapper<Script> commonWrapper = new QueryWrapper<>();
        commonWrapper.eq("type", SCRIPT_CONSTANT.TYPE.COMMON.name());
        List<ScriptVO> commonScript = list(commonWrapper)
                .stream()
                .map(script -> resolve(script, session))
                .collect(Collectors.toList());

        QueryWrapper<Script> nodeWrapper = new QueryWrapper<>();
        nodeWrapper.eq("associate_id", session.getCurrentNodeId());
        List<ScriptVO> nodeScript = list(nodeWrapper)
                .stream()
                .map(script -> resolve(script, session))
                .collect(Collectors.toList());

        List<ScriptVO> feedbackScript = new ArrayList<>();
        for (String logId: session.getOperations()) {
            OperationLog operationLog = operationLogService.getById(logId);
            QueryWrapper<Script> logWrapper = new QueryWrapper<>();
            logWrapper.eq("associate_id", operationLog.getType());
            List<Script> singleLogScriptList = list(logWrapper);
            feedbackScript.addAll(singleLogScriptList.stream().map(
                    script -> resolve(script, session, operationLog)
            ).collect(Collectors.toList()));
        }

        FullScriptVO fullScriptVO = new FullScriptVO();
        fullScriptVO.setNodeScript(nodeScript);
        fullScriptVO.setCommonScript(commonScript);
        fullScriptVO.setFeedbackScript(feedbackScript);
        aIUtils.addScore(fullScriptVO, session.getContext());
        return fullScriptVO;
    }
}
