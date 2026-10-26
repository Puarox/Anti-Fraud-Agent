package rulai.config;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.SpringAnnotationScanner;
import com.corundumstudio.socketio.protocol.JacksonJsonSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SocketIOConfig {

    @Bean
    public SocketIOServer socketIOServer() {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setHostname("localhost");

        // Socket 专用端口
        config.setPort(9092);
        // 允许跨域
        config.setOrigin("*");
        return new SocketIOServer(config);
    }

    // 启动服务
    @Bean
    public SpringAnnotationScanner springAnnotationScanner(SocketIOServer server) {
        server.start();
        return new SpringAnnotationScanner(server);
    }
}
