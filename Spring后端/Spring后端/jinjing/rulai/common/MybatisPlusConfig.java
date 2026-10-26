package rulai.common;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusPropertiesCustomizer;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {
    @Bean
    public MybatisPlusPropertiesCustomizer mybatisPlusPropertiesCustomizer() {
        return properties -> {
            GlobalConfig globalConfig = properties.getGlobalConfig();
            GlobalConfig.DbConfig dbConfig = globalConfig.getDbConfig();

            // 设置逻辑删除的字段名和值
            dbConfig.setLogicDeleteField("is_deleted");  // 数据库字段名
            dbConfig.setLogicDeleteValue("1");  // 删除时的值
            dbConfig.setLogicNotDeleteValue("0");  // 未删除时的值
        };
    }
    /**
     * 添加分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        // 单页最大1000条
        paginationInnerInterceptor.setMaxLimit(1000L);
        paginationInnerInterceptor.setDbType(DbType.MYSQL);
        // 超过页数返回第一页
        paginationInnerInterceptor.setOverflow(true);
        interceptor.addInnerInterceptor(paginationInnerInterceptor);

        return interceptor;
    }
}