package rulai.utils;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import rulai.common.APIResponse;
import rulai.entity.Node;
import rulai.pojo.Dialogue;
import rulai.service.INodeService;
import rulai.vo.session.FullScriptVO;
import rulai.vo.session.OutEdgeVO;
import rulai.vo.session.ScriptVO;
import rulai.vo.session.SessionStateVO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class AIUtils {
    private static final String CLASSIFIER_URL = "http://localhost:8091/classify";
    private static final String NAVIGATE_URL = "http://localhost:8091/navigate";
    private static final String OPTIMIZE_URL = "http://localhost:8091/optimize";

    @Autowired
    JSONUtils jsonUtils;

    @Autowired
    INodeService nodeService;

    public void addScore(FullScriptVO scriptVO, List<Dialogue> dialogue) {
        try {
            // 构建JSON请求
            HashMap<String, List> requestBody = new HashMap<>();
            requestBody.put("nodeScript", scriptVO.getNodeScript());
            requestBody.put("commonScript", scriptVO.getCommonScript());
            requestBody.put("feedbackScript", scriptVO.getFeedbackScript());
            requestBody.put("dialogue", dialogue);
            String jsonInput = jsonUtils.toJSONString(requestBody);

            HttpURLConnection conn = getHttpURLConnection(jsonInput, CLASSIFIER_URL);

            if (conn.getResponseCode() == 200) {
                // 正确读取响应内容
                String responseBody = readResponse(conn.getInputStream());
                APIResponse response = jsonUtils.fromJSONString(responseBody, APIResponse.class);

                if (response.getSuccess()) {
                    Map<String, Object> data = (Map<String, Object>) response.getData();
                    List<String> ids = (List<String>) data.get("ids");
                    List<Double> scores = (List<Double>) data.get("scores");

                    // 为nodeScript设置分数
                    for (ScriptVO script : scriptVO.getNodeScript()) {
                        for (int i = 0; i < ids.size(); i++) {
                            if (script.getId().equals(ids.get(i))) {
                                script.setScore(scores.get(i).floatValue());
                            }
                        }
                    }

                    // 为commonScript设置分数
                    for (ScriptVO script : scriptVO.getCommonScript()) {
                        for (int i = 0; i < ids.size(); i++) {
                            if (script.getId().equals(ids.get(i))) {
                                script.setScore(scores.get(i).floatValue());
                            }
                        }
                    }

                    // 为feedbackScript设置分数
                    for (ScriptVO script : scriptVO.getFeedbackScript()) {
                        for (int i = 0; i < ids.size(); i++) {
                            if (script.getId().equals(ids.get(i))) {
                                script.setScore(scores.get(i).floatValue());
                            }
                        }
                    }
                } else {
                    throw new RuntimeException("AI classification failed.");
                }
            } else {
                // 读取错误信息
                String errorBody = readResponse(conn.getErrorStream());
                throw new RuntimeException("API Error " + conn.getResponseCode() + ": " + errorBody);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Classify failed: " + e.getMessage(), e);
        }
    }

    public void addScore(SessionStateVO stateVO) {
        try {
            // 构建JSON请求
            HashMap<String, Object> requestBody = new HashMap<>();
            requestBody.put("outEdges", stateVO.getOutEdge());
            Node node = nodeService.getById(stateVO.getCurrentNodeId());
            requestBody.put("currentNodeName", node.getName());
            requestBody.put("dialogue", stateVO.getContext());

            String jsonInput = jsonUtils.toJSONString(requestBody);
            HttpURLConnection conn = getHttpURLConnection(jsonInput, NAVIGATE_URL);

            if (conn.getResponseCode() == 200) {
                // 正确读取响应内容
                String responseBody = readResponse(conn.getInputStream());
                APIResponse response = jsonUtils.fromJSONString(responseBody, APIResponse.class);

                if (response.getSuccess()) {
                    Map<String, Object> data = (Map<String, Object>) response.getData();
                    List<String> ids = (List<String>) data.get("ids");
                    List<Double> scores = (List<Double>) data.get("scores");

                    // 为nodeScript设置分数
                    for (OutEdgeVO outEdgeVO : stateVO.getOutEdge()) {
                        for (int i = 0; i < ids.size(); i++) {
                            if (outEdgeVO.getEdgeId().equals(ids.get(i))) {
                                outEdgeVO.setScore(scores.get(i).floatValue());
                            }
                        }
                    }

                } else {
                    throw new RuntimeException("AI classification failed.");
                }
            } else {
                // 读取错误信息
                String errorBody = readResponse(conn.getErrorStream());
                throw new RuntimeException("API Error " + conn.getResponseCode() + ": " + errorBody);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Classify failed: " + e.getMessage(), e);
        }
    }

    public SseEmitter optimize(SessionStateVO stateVO, String currentScript, FullScriptVO scriptVO) {
        SseEmitter emitter = new SseEmitter();
        CompletableFuture.runAsync(() -> {
            try {
                HashMap<String, Object> requestBody = new HashMap<>();
                requestBody.put("outEdges", stateVO.getOutEdge());
                Node node = nodeService.getById(stateVO.getCurrentNodeId());
                requestBody.put("currentNodeName", node.getName());
                requestBody.put("currentScript", currentScript);
                requestBody.put("nodeScript", scriptVO.getNodeScript());
                requestBody.put("commonScript", scriptVO.getCommonScript());
                requestBody.put("feedbackScript", scriptVO.getFeedbackScript());
                requestBody.put("dialogue", stateVO.getContext());

                // 4. 创建HTTP连接
                URL url = new URL(OPTIMIZE_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(60000);
                conn.setReadTimeout(180000); // 延长读取超时
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "text/event-stream"); // 接受流式响应
                conn.setDoOutput(true);
                String jsonInput = jsonUtils.toJSONString(requestBody);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                if (conn.getResponseCode() == 200) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {

                        StringBuilder fullResponse = new StringBuilder();
                        char[] buffer = new char[1024];
                        int charsRead;

                        while ((charsRead = reader.read(buffer)) != -1) {
                            String chunk = new String(buffer, 0, charsRead)
                                    .replace("\n", "№℗");
                            emitter.send(SseEmitter.event()
                                    .data(chunk));

                            fullResponse.append(chunk);
                        }
                        emitter.complete();
                    }
                } else {
                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                        StringBuilder error = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            error.append(line);
                        }
                        System.out.println("API Error: " + error);

                        emitter.completeWithError(new RuntimeException("API Error: " + error));
                    }
                }
            } catch (Exception e) {
                System.out.println("API Error: " + e);
                e.printStackTrace();
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    @NotNull
    private static HttpURLConnection getHttpURLConnection(String jsonInput, String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
//        conn.setConnectTimeout(30000); // 30秒连接超时
//        conn.setReadTimeout(60000);    // 60秒读取超时
        conn.setDoOutput(true);

        // 发送请求
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        return conn;
    }

    /**
     * 从输入流读取响应内容
     */
    private String readResponse(java.io.InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }
}