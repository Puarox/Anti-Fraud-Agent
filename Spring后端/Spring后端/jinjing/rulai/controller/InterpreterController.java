package rulai.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import rulai.annotation.AllowAnonymous;
import rulai.common.APIResponse;
import rulai.httpstatus.exceptions.BadRequestException;
import rulai.pojo.Session;
import rulai.service.IInterfaceService;
import rulai.service.ISessionService;
import rulai.utils.interpolation.ConditionInterpreter;
import rulai.utils.interpolation.InterfaceRegistry;
import rulai.weblogger.WebLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * <p>
 * 图控制器
 * </p>
 *
 * @author Mistry
 * @since 2025-12-29
 */
@Controller
@RequestMapping("/interpreter")
public class InterpreterController {

    @Autowired
    IInterfaceService interpreterService;

    @Autowired
    ISessionService sessionService;

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private ConditionInterpreter conditionInterpreter;

    /**
     * 返回所有接口
     * @param
     * @return
     */
    @WebLog
    @ResponseBody
    @GetMapping("/interface/list")
    @SuppressWarnings("rawtypes")
    @AllowAnonymous
    public APIResponse listInterface() {
        return APIResponse.success(InterfaceRegistry.getRegisteredInterfaces());
    }

    /**
     * 返回所有接口
     * @param
     * @return
     */
    @WebLog
    @ResponseBody
    @GetMapping("/call/{interfaceName}/{sessionId}")
    @SuppressWarnings("rawtypes")
    @AllowAnonymous
    public APIResponse callInterface(@PathVariable String interfaceName, @PathVariable String sessionId) {
        Session session = sessionService.getSession(sessionId);
        return APIResponse.success(interpreterService.call(interfaceName, session));
    }

    /**
     * 检查Condition是否合法
     * @param
     * @return
     */
    @WebLog
    @ResponseBody
    @PostMapping("/condition/legal")
    @SuppressWarnings("rawtypes")
    @AllowAnonymous
    public APIResponse testCondition(@RequestBody(required = false) String body) {
        if (body == null || body.isEmpty()) {
            Map<String, List<String>> map = new HashMap<>();
            List<String> list = new ArrayList<>();
            map.put("errors", list);
            map.put("warnings", list);
            return APIResponse.success(map);
        }
        try {
            List<Map<String, Object>> conditions = objectMapper.readValue(body, List.class);
            conditionInterpreter.check(conditions);
            return APIResponse.success(conditionInterpreter.check(conditions));
        } catch (JsonProcessingException e) {
            throw new BadRequestException("条件结构错误！");
        }
    }

    /**
     * 检查模板是否合法
     * @param
     * @return
     */
    @WebLog
    @ResponseBody
    @PostMapping("/template/legal")
    @SuppressWarnings("rawtypes")
    @AllowAnonymous
    public APIResponse testTemplate(@RequestBody(required = false) String body) {
        if (body == null || body.isEmpty()) {
            Map<String, List<String>> map = new HashMap<>();
            List<String> list = new ArrayList<>();
            map.put("errors", list);
            return APIResponse.success(map);
        }
        return APIResponse.success(interpreterService.check(body));
    }
}
