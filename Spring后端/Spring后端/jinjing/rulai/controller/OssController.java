package rulai.controller;

import org.springframework.web.bind.annotation.*;
import rulai.service.impl.OssService;
import rulai.weblogger.WebLog;

import java.util.Map;

@RestController
@RequestMapping("/oss")
public class OssController {

    private final OssService ossService;

    public OssController() throws Exception {
        this.ossService = new OssService();
    }

    /**
     * 获取上传签名URL（通用）
     */
    @WebLog
    @GetMapping("/sign-url")
    public Map<String, String> getUploadUrl(
            @RequestParam(defaultValue = "jpg") String suffix
    ) {
        return ossService.generateUploadUrl(suffix);
    }
}