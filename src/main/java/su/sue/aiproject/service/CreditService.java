package su.sue.aiproject.service;

import java.math.BigDecimal;

/**
 * 积分服务接口
 */
public interface CreditService {
    
    /**
     * 获取用户积分余额
     * 
     * @param userId 用户ID
     * @return 积分余额
     */
    BigDecimal getUserCredit(Long userId);
    
    /**
     * 扣除用户积分
     * 
     * @param userId 用户ID
     * @param amount 扣除金额
     * @param description 扣除描述
     */
    void deductCredit(Long userId, BigDecimal amount, String description);
    
    /**
     * 增加用户积分
     * 
     * @param userId 用户ID
     * @param amount 增加金额
     * @param description 增加描述
     */
    void addCredit(Long userId, BigDecimal amount, String description);
    
    /**
     * 检查用户积分是否足够
     * 
     * @param userId 用户ID
     * @param amount 需要的金额
     * @return 是否足够
     */
    boolean hasEnoughCredit(Long userId, BigDecimal amount);
}
