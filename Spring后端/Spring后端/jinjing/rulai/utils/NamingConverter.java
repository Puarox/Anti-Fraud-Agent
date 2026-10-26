package rulai.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * 驼峰和盘蛇式相互转化工具类
 * </p>
 *
 * @author Mistry
 * @since 2025-06-24
 */

public class NamingConverter {
    
    // 驼峰转蛇形（支持数字和特殊字符）
    public static String camelToSnake(String str) {
        if (str == null) {
            return null;
        }
        
        // 正则表达式：在大写字母前插入下划线（首字母除外）
        String regex = "(?<=[a-z0-9])(?=[A-Z])";
        String snake = str.replaceAll(regex, "_");
        
        // 将所有字母转为小写
        return snake.toLowerCase();
    }

    // 蛇形转驼峰（支持数字和特殊字符）
    public static String snakeToCamel(String str) {
        if (str == null) {
            return null;
        }
        
        Pattern pattern = Pattern.compile("_([a-z0-9])");
        Matcher matcher = pattern.matcher(str.toLowerCase());
        StringBuffer buffer = new StringBuffer();
        
        while (matcher.find()) {
            matcher.appendReplacement(buffer, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(buffer);
        
        return buffer.toString();
    }
}