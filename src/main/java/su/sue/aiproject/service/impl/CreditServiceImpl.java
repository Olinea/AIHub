package su.sue.aiproject.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import su.sue.aiproject.domain.Users;
import su.sue.aiproject.service.CreditService;
import su.sue.aiproject.service.UsersService;

import java.math.BigDecimal;

/**
 * 积分服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreditServiceImpl implements CreditService {
    
    private final UsersService usersService;
    
    @Override
    public BigDecimal getUserCredit(Long userId) {
        Users user = usersService.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user.getCreditBalance() != null ? user.getCreditBalance() : BigDecimal.ZERO;
    }
    
    @Override
    @Transactional
    public void deductCredit(Long userId, BigDecimal amount, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("扣除金额必须大于0");
        }
        
        // 检查余额
        if (!hasEnoughCredit(userId, amount)) {
            throw new RuntimeException("积分余额不足");
        }
        
        // 扣除积分
        UpdateWrapper<Users> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", userId)
                    .setSql("credit_balance = credit_balance - " + amount);
        
        boolean success = usersService.update(updateWrapper);
        if (!success) {
            throw new RuntimeException("扣除积分失败");
        }
        
        log.info("用户 {} 扣除积分 {}, 描述: {}", userId, amount, description);
        
        // TODO: 记录积分变更日志
    }
    
    @Override
    @Transactional
    public void addCredit(Long userId, BigDecimal amount, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("增加金额必须大于0");
        }
        
        UpdateWrapper<Users> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", userId)
                    .setSql("credit_balance = credit_balance + " + amount);
        
        boolean success = usersService.update(updateWrapper);
        if (!success) {
            throw new RuntimeException("增加积分失败");
        }
        
        log.info("用户 {} 增加积分 {}, 描述: {}", userId, amount, description);
        
        // TODO: 记录积分变更日志
    }
    
    @Override
    public boolean hasEnoughCredit(Long userId, BigDecimal amount) {
        BigDecimal userCredit = getUserCredit(userId);
        return userCredit.compareTo(amount) >= 0;
    }
}
