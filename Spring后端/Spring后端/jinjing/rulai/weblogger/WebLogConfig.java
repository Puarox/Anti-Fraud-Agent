package rulai.weblogger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * 日志模块注册
 * </p>
 *
 * @author Mistry
 * @since 2025-06-23
 */

@Configuration
public class WebLogConfig {
    @Bean
    public WebLogAspect webLogAspect() {
        return new WebLogAspect();
    }
}