package rulai.service;

/**
 * <p>
 * UDP 语音 服务类
 * </p>
 *
 * @author LanZhan-Harmony
 * @since 2026-1-13
 */
public interface IAudioService {
    void init();

    void stop();

    void serverHangUp(String clientIp, int clientPort, String phone);
}
