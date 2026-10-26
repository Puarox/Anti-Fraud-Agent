package rulai.service;

import org.springframework.stereotype.Service;

import java.io.IOException;


public interface IASRService {
    public boolean startASRScript(String sessionId);
}
