package su.sue.aiproject.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * API密钥加密解密服务
 * 用于安全存储AI厂商的API密钥
 */
@Service
public class ApiKeyEncryptionService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";

    @Value("${app.encryption.secret-key:MySecretKey123456}")
    private String secretKey;

    /**
     * 加密API密钥
     * @param apiKey 原始API密钥
     * @return 加密后的API密钥
     */
    public String encrypt(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return null;
        }
        
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            
            byte[] encryptedBytes = cipher.doFinal(apiKey.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("API密钥加密失败", e);
        }
    }

    /**
     * 解密API密钥
     * @param encryptedApiKey 加密的API密钥
     * @return 原始API密钥
     */
    public String decrypt(String encryptedApiKey) {
        if (encryptedApiKey == null || encryptedApiKey.trim().isEmpty()) {
            return null;
        }
        
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedApiKey);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("API密钥解密失败", e);
        }
    }

    /**
     * 生成随机密钥（用于初始化配置）
     * @return Base64编码的密钥
     */
    public static String generateRandomKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(128, new SecureRandom());
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("密钥生成失败", e);
        }
    }
}
