package su.sue.aiproject.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import su.sue.aiproject.domain.*;
import su.sue.aiproject.security.JwtTokenProvider;
import su.sue.aiproject.service.UsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

// 添加日志依赖
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证接口", description = "用户认证相关接口")
@Validated
public class AuthController {
    
    // 添加日志记录器
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UsersService usersService;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用邮箱和密码进行登录")
    public ResponseEntity<ApiResponse<JwtAuthenticationResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            logger.info("登录尝试 - 邮箱: {}", loginRequest.getEmail());
            
            // 检查用户是否存在
            try {
                usersService.loadUserByUsername(loginRequest.getEmail());
                logger.info("用户存在，开始认证");
            } catch (Exception e) {
                logger.error("用户不存在: {}", e.getMessage());
                return ResponseEntity.badRequest().body(ApiResponse.error(400, "用户不存在"));
            }
            
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
            
            logger.info("认证成功");

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = tokenProvider.generateToken(authentication);
            Long expiresIn = tokenProvider.getJwtExpirationInMs() / 1000; // 转换为秒
            
            // 获取用户信息
            Users user = usersService.getUserByEmail(loginRequest.getEmail());
            UserInfo userInfo = UserInfo.fromUsers(user);
            
            JwtAuthenticationResponse response = new JwtAuthenticationResponse(jwt, expiresIn, loginRequest.getEmail(), userInfo);
            
            logger.info("Token生成成功，登录完成");
            return ResponseEntity.ok(ApiResponse.success("登录成功", response));
        } catch (Exception e) {
            logger.error("登录失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "邮箱或密码错误: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "注册新用户账号")
    public ResponseEntity<ApiResponse<String>> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        if (usersService.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "邮箱已被使用"));
        }

        usersService.registerUser(registerRequest);

        return ResponseEntity.ok(ApiResponse.success("用户注册成功", "注册完成，请登录"));
    }
    
    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的信息并重置JWT")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<JwtAuthenticationResponse>> getCurrentUser() {
        try {
            // 从SecurityContext获取当前认证信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(ApiResponse.error(401, "未认证"));
            }
            
            String email = authentication.getName();
            logger.info("获取用户信息 - 邮箱: {}", email);
            
            // 获取用户信息
            Users user = usersService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.status(404).body(ApiResponse.error(404, "用户不存在"));
            }
            
            // 生成新的JWT token
            String jwt = tokenProvider.generateToken(authentication);
            Long expiresIn = tokenProvider.getJwtExpirationInMs() / 1000; // 转换为秒
            
            UserInfo userInfo = UserInfo.fromUsers(user);
            JwtAuthenticationResponse response = new JwtAuthenticationResponse(jwt, expiresIn, email, userInfo);
            
            logger.info("用户信息获取成功，新Token生成完成");
            return ResponseEntity.ok(ApiResponse.success("获取用户信息成功", response));
        } catch (Exception e) {
            logger.error("获取用户信息失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "获取用户信息失败: " + e.getMessage()));
        }
    }
    
    // 管理员接口 - 需要检查管理员权限
    private boolean isCurrentUserAdmin() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return false;
            }
            String email = authentication.getName();
            Users user = usersService.getUserByEmail(email);
            return user != null && user.getIsAdmin() != null && user.getIsAdmin() == 1;
        } catch (Exception e) {
            logger.error("检查管理员权限失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @GetMapping("/admin/users")
    @Operation(summary = "获取所有用户", description = "管理员获取所有用户列表（分页）")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Page<UserInfo>>> getAllUsers(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        try {
            if (!isCurrentUserAdmin()) {
                return ResponseEntity.status(403).body(ApiResponse.error(403, "权限不足，只有管理员可以访问"));
            }
            
            Page<Users> usersPage = usersService.getAllUsers(current, size);
            Page<UserInfo> userInfoPage = new Page<>(current, size);
            userInfoPage.setTotal(usersPage.getTotal());
            userInfoPage.setRecords(
                usersPage.getRecords().stream()
                    .map(UserInfo::fromUsers)
                    .collect(Collectors.toList())
            );
            
            return ResponseEntity.ok(ApiResponse.success("获取用户列表成功", userInfoPage));
        } catch (Exception e) {
            logger.error("获取用户列表失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "获取用户列表失败: " + e.getMessage()));
        }
    }
    
    @PutMapping("/admin/users/admin")
    @Operation(summary = "更新用户管理员状态", description = "管理员更新用户的管理员权限")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<String>> updateUserAdmin(@Valid @RequestBody UpdateUserAdminRequest request) {
        try {
            if (!isCurrentUserAdmin()) {
                return ResponseEntity.status(403).body(ApiResponse.error(403, "权限不足，只有管理员可以访问"));
            }
            
            boolean success = usersService.updateUserAdmin(request.getUserId(), request.getIsAdmin());
            if (success) {
                String status = request.getIsAdmin() == 1 ? "管理员" : "普通用户";
                return ResponseEntity.ok(ApiResponse.success("用户权限更新成功", "已将用户设置为" + status));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error(400, "更新失败，用户不存在"));
            }
        } catch (Exception e) {
            logger.error("更新用户管理员状态失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "更新失败: " + e.getMessage()));
        }
    }
    
    @PutMapping("/admin/users/credit")
    @Operation(summary = "更新用户积分", description = "管理员更新用户的积分余额")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<String>> updateUserCredit(@Valid @RequestBody UpdateUserCreditRequest request) {
        try {
            if (!isCurrentUserAdmin()) {
                return ResponseEntity.status(403).body(ApiResponse.error(403, "权限不足，只有管理员可以访问"));
            }
            
            boolean success = usersService.updateUserCredit(request.getUserId(), request.getCreditBalance());
            if (success) {
                return ResponseEntity.ok(ApiResponse.success("用户积分更新成功", "积分已更新为: " + request.getCreditBalance()));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error(400, "更新失败，用户不存在"));
            }
        } catch (Exception e) {
            logger.error("更新用户积分失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "更新失败: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/admin/users/{userId}")
    @Operation(summary = "删除用户", description = "管理员删除用户")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long userId) {
        try {
            if (!isCurrentUserAdmin()) {
                return ResponseEntity.status(403).body(ApiResponse.error(403, "权限不足，只有管理员可以访问"));
            }
            
            // 不允许删除当前登录的管理员自己
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserEmail = authentication.getName();
            Users currentUser = usersService.getUserByEmail(currentUserEmail);
            if (currentUser != null && currentUser.getId().equals(userId)) {
                return ResponseEntity.badRequest().body(ApiResponse.error(400, "不能删除自己的账户"));
            }
            
            boolean success = usersService.deleteUser(userId);
            if (success) {
                return ResponseEntity.ok(ApiResponse.success("用户删除成功", "用户已被删除"));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error(400, "删除失败，用户不存在"));
            }
        } catch (Exception e) {
            logger.error("删除用户失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "删除失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/admin/users/search")
    @Operation(summary = "搜索用户", description = "管理员根据关键字搜索用户")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<UserInfo>>> searchUsers(@RequestParam String keyword) {
        try {
            if (!isCurrentUserAdmin()) {
                return ResponseEntity.status(403).body(ApiResponse.error(403, "权限不足，只有管理员可以访问"));
            }
            
            List<Users> users = usersService.searchUsers(keyword);
            List<UserInfo> userInfos = users.stream()
                .map(UserInfo::fromUsers)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success("搜索完成", userInfos));
        } catch (Exception e) {
            logger.error("搜索用户失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "搜索失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/admin/users/summary")
    @Operation(summary = "获取用户统计", description = "管理员获取用户总数、新增用户、活跃用户等统计信息")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<UsersSummaryResponse>> getUsersSummary() {
        try {
            if (!isCurrentUserAdmin()) {
                return ResponseEntity.status(403).body(ApiResponse.error(403, "权限不足，只有管理员可以访问"));
            }
            
            UsersSummaryResponse summary = usersService.getUsersSummary();
            return ResponseEntity.ok(ApiResponse.success("获取用户统计成功", summary));
        } catch (Exception e) {
            logger.error("获取用户统计失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "获取用户统计失败: " + e.getMessage()));
        }
    }
}
