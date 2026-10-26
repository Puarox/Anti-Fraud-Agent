package rulai.utils.interpolation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 字符串插值工具类
 * 用于解析字符串中的 {interfaceName} 并替换为接口调用的结果
 * 使用 {{ 和 }} 表示原始的花括号字符
 * </p>
 *
 * @author Mistry
 * @since 2026-01-06
 */

@Slf4j
@Component
public class InterpolationUtil {
    /**
     * 转义双花括号：将 {{ 和 }} 替换为临时标记
     */
    public static String escapeDoubleBraces(String input) {
        String step1 = input.replace("{{{{", "\\{\\{\\{\\{");
        String step2 = step1.replace("}}}}", "\\}\\}\\}\\}");
        
        String step3 = step2.replace("{{", "__LEFT_BRACE__");
        
        String step4 = step3.replace("}}", "__RIGHT_BRACE__");
        return step4.replace("\\{\\{\\{\\{", "{{{{")
                    .replace("\\}\\}\\}\\}", "}}}}");
    }
    
    /**
     * 恢复双花括号：将临时标记替换回 {{ 和 }}
     */
    public static String restoreDoubleBraces(String input) {
        return input.replace("__LEFT_BRACE__", "{")
                    .replace("__RIGHT_BRACE__", "}");
    }
}