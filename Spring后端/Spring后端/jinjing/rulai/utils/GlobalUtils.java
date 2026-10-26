package rulai.utils;

import org.springframework.stereotype.Component;
import rulai.httpstatus.exceptions.ServiceUnavailableException;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class GlobalUtils {
    public static String getCryptedPassword(String password, String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        try {
            // 将用户名作为盐值的一部分
            String salt = generateSaltFromUsername(username);

            // 组合密码和盐值
            String saltedPassword = password + "{" + salt + "}";

            // 使用SHA-256哈希算法
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(saltedPassword.getBytes(StandardCharsets.UTF_8));

            // 将字节数组转换为Base64字符串
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256算法不可用", e);
        } catch (Exception e) {
            throw new ServiceUnavailableException("密码加密失败", e);
        }
    }

    private static String generateSaltFromUsername(String username) {
        return new StringBuilder(username)
                .reverse() + "|" + System.getenv("SYSTEM_PEPPER");
    }
}
