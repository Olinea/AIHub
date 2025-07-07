package su.sue.aiproject.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
// import org.springframework.web.bind.annotation.RestControllerAdvice;
import su.sue.aiproject.domain.ApiResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 注意：由于Spring Boot 3.5.3与Knife4j 4.5.0的兼容性问题，
 * 暂时禁用@RestControllerAdvice注解以避免ControllerAdviceBean构造函数冲突。
 * 
 * 解决方案：
 * 1. 等待Knife4j发布兼容Spring Boot 3.5.x的版本
 * 2. 或者降级Spring Boot到3.3.x版本
 * 3. 或者使用纯SpringDoc OpenAPI替代Knife4j
 */
// @RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, "输入数据验证失败"));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleBindExceptions(
            BindException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, "输入数据验证失败"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "服务器内部错误"));
    }
}
