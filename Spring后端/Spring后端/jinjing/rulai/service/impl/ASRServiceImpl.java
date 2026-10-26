package rulai.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import rulai.service.IASRService;

import java.io.IOException;

@Service
public class ASRServiceImpl implements IASRService {
    @Value("${asr.python.exe}")
    private String pythonExe ;
    @Value("${asr.python.script}")
    private String script ;
    @Override
    public boolean startASRScript(String sessionId) {
        try{
        new ProcessBuilder(
                "cmd.exe",
                "/c",
                "start",
                "",
                "cmd",
                "/k",
                pythonExe,
                script,
                sessionId
        ).start();}
        catch (IOException e){
            throw new RuntimeException();
        }

        System.out.println("Python ASR 已在前台 CMD 窗口启动");
        return false;
    }
}
