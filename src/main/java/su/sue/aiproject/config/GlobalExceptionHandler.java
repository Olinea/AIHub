package su.sue.aiproject.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import su.sue.aiproject.domain.ApiResponse;

/**
 * 全局异常处理器
 * 处理所有异常，确保返回统一的ApiResponse格式
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        StringBuilder errorMessage = new StringBuilder();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String message = error.getDefaultMessage();
            if (errorMessage.length() > 0) {
                errorMessage.append("; ");
            }
            errorMessage.append(message);
        });
        
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, errorMessage.toString()));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<String>> handleBindExceptions(
            BindException ex) {
        StringBuilder errorMessage = new StringBuilder();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String message = error.getDefaultMessage();
            if (errorMessage.length() > 0) {
                errorMessage.append("; ");
            }
            errorMessage.append(message);
        });
        
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, errorMessage.toString()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "服务器内部错误"));
    }
}
