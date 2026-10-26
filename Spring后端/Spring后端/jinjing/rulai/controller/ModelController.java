package rulai.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import rulai.common.APIResponse;
import rulai.entity.FraudCases;
import rulai.httpstatus.exceptions.ResourceNotFoundException;
import rulai.service.impl.FraudCasesServiceImpl;
import rulai.vo.AnalysisResultVO;
import rulai.weblogger.WebLog;

import java.io.File;
import java.util.*;

/**
 * 模型控制器（适配 Flask 多模态服务）
 */
@RestController
@RequestMapping("/model")
public class ModelController {

    private static final Logger log = LoggerFactory.getLogger(ModelController.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private FraudCasesServiceImpl fraudCasesService;

    // Flask 服务地址
    private static final String FLASK_BASE_URL = "http://127.0.0.1:5000";

    // =========================
    // 通用方法
    // =========================

    /**
     * 统一POST请求（JSON）
     */
    private Map postForMap(String url, Object requestBody, MediaType contentType) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(contentType);

        HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(url, entity, Map.class);

        return response.getBody();
    }

    /**
     * 统一解析 Flask 返回结果
     */
    private AnalysisResultVO parseResult(Map body) {

        if (body == null) {
            throw new RuntimeException("Flask服务无响应");
        }

        Map resultMap = (Map) body.get("result");
        if (resultMap == null) {
            throw new RuntimeException("返回数据缺少 result 字段");
        }

        AnalysisResultVO vo = new AnalysisResultVO();

        try {
            String isFraudStr = (String) resultMap.getOrDefault("is_fraud", "SUSPICIOUS");
            vo.setIsFraud(
                    AnalysisResultVO.FraudStatus.valueOf(isFraudStr.toUpperCase())
            );

            vo.setFraudType((String) resultMap.get("fraud_type"));

            String riskLevelStr = (String) resultMap.getOrDefault("risk_level", "MEDIUM");
            vo.setRiskLevel(
                    AnalysisResultVO.RiskLevel.valueOf(riskLevelStr.toUpperCase())
            );

            vo.setReason((String) resultMap.get("reason"));
            vo.setAdvice((String) resultMap.get("advice"));

            vo.setCaseId((String) body.get("case_id"));

        } catch (Exception e) {
            throw new RuntimeException("数据解析失败：" + e.getMessage());
        }

        return vo;
    }

    // =========================
    // 1. 文本诊断p
    // =========================
    @WebLog
    @PostMapping("/text")
    public APIResponse<AnalysisResultVO> diagnoseText(
            @RequestBody Map<String, String> request
    ) {

        Map body = postForMap(
                FLASK_BASE_URL + "/diagnose/text",
                request,
                MediaType.APPLICATION_JSON
        );

        return APIResponse.success(parseResult(body));
    }

    // =========================
    // 2. 图片 + 文本
    // =========================
    @WebLog
    @PostMapping("/image")
    public APIResponse<AnalysisResultVO> diagnoseImage(
            @RequestBody Map<String, String> request
    ) {

        Map body = postForMap(
                FLASK_BASE_URL + "/diagnose/image",
                request,
                MediaType.APPLICATION_JSON
        );

        return APIResponse.success(parseResult(body));
    }

    // =========================
    // 3. 音频 + 文本
    // =========================
    @WebLog
    @PostMapping("/audio")
    public APIResponse<AnalysisResultVO> diagnoseAudio(
            @RequestBody Map<String, String> request
    ) {

        Map body = postForMap(
                FLASK_BASE_URL + "/diagnose/audio",
                request,
                MediaType.APPLICATION_JSON
        );

        return APIResponse.success(parseResult(body));
    }

    // =========================
    // 4. 视频 + 文本（multipart）
    // =========================
    @WebLog
    @PostMapping("/video")
    public APIResponse<AnalysisResultVO> diagnoseVideo(
            @RequestBody Map<String, String> request
    ) {

        String text = request.getOrDefault("text", "");
        String videoUrl = request.get("videoUrl");

        if (videoUrl == null || videoUrl.isEmpty()) {
            throw new IllegalArgumentException("videoUrl不能为空");
        }

        String url = FLASK_BASE_URL + "/diagnose/video";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("text", text);
        body.put("video_url", videoUrl);

        HttpEntity<Map<String, String>> requestEntity =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(url, requestEntity, Map.class);

        return APIResponse.success(parseResult(response.getBody()));
    }

    // =========================
    // 5. RAG 初始化
    // =========================
    @PostMapping("/rag/init")
    public ResponseEntity<?> initRag() {

        try {
            // 1️⃣ 查询数据库
            List<FraudCases> list = fraudCasesService.getAllFraudCases();

            if (list == null || list.isEmpty()) {
                return ResponseEntity.badRequest().body("数据库无数据");
            }

            // 2️⃣ 构造 cases
            List<Map<String, String>> cases = new ArrayList<>();

            for (FraudCases fc : list) {

                if (fc.getCaseId() == null || fc.getRagText() == null) {
                    continue;
                }

                Map<String, String> map = new HashMap<>();
                map.put("case_id", fc.getCaseId());
                map.put("rag_text", fc.getRagText());

                cases.add(map);
            }

            // 3️⃣ 请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("cases", cases);

            // 4️⃣ HTTP请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity =
                    new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response =
                    restTemplate.postForEntity(
                            FLASK_BASE_URL + "/diagnose/rag/init",
                            entity,
                            String.class
                    );

            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("RAG初始化失败: " + e.getMessage());
        }
    }
}