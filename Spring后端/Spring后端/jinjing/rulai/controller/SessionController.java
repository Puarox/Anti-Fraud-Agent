package rulai.controller;

import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.qcloud.cos.utils.Jackson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.websocket.server.PathParam;
import net.sf.jsqlparser.statement.create.table.Index;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import rulai.annotation.AllowAnonymous;
import rulai.annotation.AutoLog;
import rulai.common.APIResponse;
import rulai.constant.SESSION_CONSTANT;
import rulai.convertor.ISessionConverter;
import rulai.entity.Edge;
import rulai.entity.Node;
import rulai.entity.OperationLog;
import rulai.entity.Record;
import rulai.httpstatus.exceptions.BadRequestException;
import rulai.httpstatus.exceptions.ResourceNotFoundException;
import rulai.pojo.Dialogue;
import rulai.pojo.Session;
import rulai.service.*;
import rulai.utils.AIUtils;
import rulai.utils.JSONUtils;
import rulai.vo.session.FullScriptVO;
import rulai.vo.session.SessionStateVO;
import rulai.vo.DialogueVO;
import rulai.weblogger.WebLog;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


/**
 * <p>
 * 服务状态控制器
 * </p>
 *
 * @author Mistry
 * @since 2025-12-31
 */
@Controller
@RequestMapping("/session")
public class SessionController {
    @Autowired
    IScriptService scriptService;

    @Autowired
    INodeService nodeService;

    @Autowired
    ISessionService sessionService;

    @Autowired
    IEdgeService edgeService;

    @Autowired
    IRecordService recordService;

    @Autowired
    ISessionConverter sessionConverter;

    @Autowired
    IOperationLogService operationLogService;

    @Autowired
    IASRService asrService;
    @Autowired
    JSONUtils jsonUtils;
    @Autowired
    private AIUtils aiUtils;

    @Autowired
    private SocketIOServer socketIOServer;
    /**
     * 创建新会话
     * @param customerId
     * @return
     */
    @WebLog
    @ResponseBody
    @PostMapping("/start")
    @SuppressWarnings("rawtypes")
    @AllowAnonymous
    @ResponseStatus(HttpStatus.CREATED)
    public APIResponse createSession(@RequestParam String customerId,
                                     @RequestHeader(value = "userId") String userId,
                                     HttpServletRequest request) throws JsonProcessingException {
        Node startNode = nodeService.getStart();

        Session session = new Session();
        String sessionId = UUID.randomUUID().toString();
        session.setId(sessionId);

        //在此启动用于演示的实时通话转文本的python脚本
        asrService.startASRScript(sessionId);

        session.setCustomerId(customerId);
        session.setUserId(userId);
        session.setCurrentNodeId(startNode.getId());
        session.setStartTime(LocalDateTime.now());
        session.setStatus(SESSION_CONSTANT.STATE.IN_PROGRESS.name());
        session.start(startNode);

        Dialogue  dialogue = new Dialogue();
        dialogue.setRole("system");
        dialogue.setContent("客户发起了新会话");
        dialogue.setTime(session.getStartTime());
        session.dialogue(dialogue);

        sessionService.setSession(session.getId(), session);

        OperationLog operationLog = new OperationLog();
        operationLog.setId(UUID.randomUUID().toString());
        operationLog.setUserId(userId);
        operationLog.setType("OTHER");
        operationLog.setTableName("record");
        operationLog.setUrl(request.getRequestURL().toString());

        List<String> requestList = new ArrayList<>();
        requestList.add(request.getQueryString());
        requestList.add(userId);
        operationLog.setRequest(jsonUtils.toJSONString(requestList));
        operationLog.setRespond(jsonUtils.toJSONString(session));
        operationLog.setDescription("createSession");
        operationLog.setCreatedAt(LocalDateTime.now());
        operationLogService.save(operationLog);
        sessionService.addOperation(session.getId(), operationLog.getId());

        return APIResponse.success(session);
    }

    /**
     * 结束会话并写入数据库
     * @param sessionId
     * @return
     */
    @WebLog
    @ResponseBody
    @PutMapping("/{sessionId}/end")
    @SuppressWarnings("rawtypes")
    @AllowAnonymous
    @ResponseStatus(HttpStatus.OK)
    @AutoLog(type = "CREATE", tableName = "record")
    public APIResponse endSession(@PathVariable String sessionId) {
        Node endNode = nodeService.getEnd();
        Session session = sessionService.getSession(sessionId);

        if(session == null) {
            throw new ResourceNotFoundException("会话不存在");
        }
        Record record = sessionConverter.toRecord(session);
        record.setEndTime(LocalDateTime.now());
        recordService.save(record);
        sessionService.deleteSession(session.getId());
        session.end(endNode);
        return APIResponse.success(recordService.getById(record.getId()));
    }

    /**
     * 删除会话
     * @param sessionId
     * @return
     */
    @WebLog
    @ResponseBody
    @DeleteMapping("/{sessionId}")
    @SuppressWarnings("rawtypes")
    public APIResponse deleteSession(@PathVariable String sessionId){

        if(sessionService.getSession(sessionId) == null) {
            throw new ResourceNotFoundException("会话不存在");

        }
        sessionService.deleteSession(sessionId);
        return APIResponse.success();
    }

    /**
     * 根据ID获取会话信息
     * @param sessionId
     * @return
     */
    @WebLog
    @ResponseBody
    @GetMapping("/{sessionId}")
    @SuppressWarnings("rawtypes")
    @AllowAnonymous
    public APIResponse getSession(@PathVariable String sessionId) {
        Session session = sessionService.getSession(sessionId);
        if(session == null) {
            throw new ResourceNotFoundException("会话不存在");
        }
        return APIResponse.success(session);
    }

    /**
     * 根据边ID对会话节点进行转移
     * @param sessionId
     * @param edgeId
     * @return
     */
    @WebLog
    @ResponseBody
    @PutMapping("/{sessionId}/transfer/{edgeId}")
    @SuppressWarnings("rawtypes")
    @AllowAnonymous
    @ResponseStatus(HttpStatus.OK)
    public APIResponse transferSession(@PathVariable String sessionId, @PathVariable String edgeId) {
        Session session = sessionService.getSession(sessionId);
        if(session == null) {
            throw new ResourceNotFoundException("会话不存在");
        }

        Edge edge = edgeService.getById(edgeId);
        Node nxtNode = nodeService.getById(edge.getTargetId());
        session.forward(edge, nxtNode);
        session.setCurrentNodeId(nxtNode.getId());
        sessionService.setSession(session.getId(), session);
        return APIResponse.success(session);
    }

    /**
     * 将会话回退至上一步，即 撤销
     * @param sessionId
     * @return
     */
    @WebLog
    @ResponseBody
    @PutMapping("/{sessionId}/undo")
    @SuppressWarnings("rawtypes")
    @AllowAnonymous
    @ResponseStatus(HttpStatus.OK)
    public APIResponse undo(@PathVariable String sessionId) {
        Session session = sessionService.getSession(sessionId);
        if(session == null) {
            throw new ResourceNotFoundException("会话不存在");
        }
        try{
            session.undo();
        } catch(IndexOutOfBoundsException e){
            throw new BadRequestException(e.getMessage());
        }
        sessionService.setSession(session.getId(), session);
        return APIResponse.success(session);
    }

    /**
     * 将会话重做至下一步，撤销的逆操作，即 重做
     * @param sessionId
     * @return
     */
    @WebLog
    @ResponseBody
    @PutMapping("/{sessionId}/redo")
    @SuppressWarnings("rawtypes")
    @AllowAnonymous
    @ResponseStatus(HttpStatus.OK)
    public APIResponse redo(@PathVariable String sessionId) {
        Session session = sessionService.getSession(sessionId);
        if(session == null) {
            throw new ResourceNotFoundException("会话不存在");
        }
        try{
            session.redo();
        } catch(IndexOutOfBoundsException e){
            throw new BadRequestException(e.getMessage());
        }
        sessionService.setSession(session.getId(), session);
        return APIResponse.success(session);
    }

    /**
     * 获取当前会话的所有话术
     * @param sessionId
     * @return
     */
    @WebLog
    @ResponseBody
    @GetMapping("/{sessionId}/scripts")
    @SuppressWarnings("rawtypes")
    @AllowAnonymous
    public APIResponse getScript(@PathVariable String sessionId) {
        Session session = sessionService.getSession(sessionId);
        if(session == null) {
            throw new ResourceNotFoundException("会话不存在");
        }
        return APIResponse.success(scriptService.getScriptBySession(session));
    }

    /**
     * 获取当前会话的状态信息
     * @param sessionId
     * @return
     */
    @WebLog
    @ResponseBody
    @GetMapping("/{sessionId}/state")
    @SuppressWarnings("rawtypes")
    @AllowAnonymous
    public APIResponse getState(@PathVariable String sessionId) {
        Session session = sessionService.getSession(sessionId);
        if(session == null) {
            throw new ResourceNotFoundException("会话不存在");
        }
        return APIResponse.success(sessionService.getStateBySession(session));
    }


    @WebLog
    @ResponseBody
    @PostMapping(value = "/{sessionId}/optimize", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter optimize(@RequestParam String currentScript,
                               @PathVariable String sessionId) {
        Session session = sessionService.getSession(sessionId);
        SessionStateVO sessionStateVO = sessionService.getStateBySession(session);
        FullScriptVO fullScriptVO = scriptService.getScriptBySession(session);
        return aiUtils.optimize(sessionStateVO, currentScript, fullScriptVO);
    }

    /**
     * 更新对话内容
     */
    @WebLog
    @ResponseBody
    @PostMapping("/{sessionId}/dialogue")
    @AllowAnonymous
    public APIResponse dialogue(@PathVariable String sessionId, Dialogue dialogue) {
        Session session = sessionService.getSession(sessionId);
        if(session == null) {
            throw new ResourceNotFoundException("会话不存在");
        }
        session.dialogue(dialogue);
        sessionService.setSession(session.getId(), session);
        DialogueVO dialogueVO = new DialogueVO();
        dialogueVO.setRole(dialogue.getRole());
        dialogueVO.setContent(dialogue.getContent());
        dialogueVO.setTime(
                dialogue.getTime() == null
                        ? null
                        : dialogue.getTime().toString()
        );
        socketIOServer.getRoomOperations(sessionId).sendEvent("new_message", dialogueVO);
        return APIResponse.success(session.getContext());
    }

    /**
     * 根据sessionid获取上下文
     * @param sessionId
     * @return
     */
    @WebLog
    @ResponseBody
    @GetMapping("/{sessionId}/context")
    @SuppressWarnings("rawtypes")
    @AllowAnonymous
    public APIResponse getContext(@PathVariable String sessionId) {
        Session session = sessionService.getSession(sessionId);
        if(session == null) {
            throw new ResourceNotFoundException("会话不存在");
        }
        return APIResponse.success(scriptService.getContextBySession(session));
    }
}
