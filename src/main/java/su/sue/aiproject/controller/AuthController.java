package su.sue.aiproject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import su.sue.aiproject.domain.LoginRequest;
import su.sue.aiproject.domain.RegisterRequest;
import su.sue.aiproject.domain.ApiResponse;
import su.sue.aiproject.domain.JwtAuthenticationResponse;
import su.sue.aiproject.domain.UserInfo;
import su.sue.aiproject.domain.Users;
import su.sue.aiproject.security.JwtTokenProvider;
import su.sue.aiproject.service.UsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

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
        if (usersService.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "用户名已被使用"));
        }

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
}
