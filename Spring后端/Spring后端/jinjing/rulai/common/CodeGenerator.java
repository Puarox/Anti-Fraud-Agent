package rulai.common;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.nio.file.Paths;

public class CodeGenerator {
    public static void main(String[] args) {
        generateAllOverwrite();
    }
    public static void generateAllOverwrite()
    {
        FastAutoGenerator.create("jdbc:mysql://localhost:3306/deeputy?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true",
                        "root", "newpassword")
                .globalConfig(builder -> builder
                        .author("Mistry")
                        .outputDir(Paths.get(System.getProperty("user.dir")) + "/src/main/java")
                        .commentDate("yyyy-MM-dd")
                )
                .packageConfig(builder -> builder
                        .parent("rulai")
                        .entity("entity")
//                        .mapper("mapper")
//                        .service("service")
//                        .serviceImpl("service.impl")
//                        .xml("mapper.xml")
                )
                .strategyConfig(builder -> builder
                        .addInclude()
                        .entityBuilder()
                        .enableLombok()
                        .enableFileOverride()
                        .mapperBuilder()
                        .enableFileOverride()

                )
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }

}
