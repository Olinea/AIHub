# Knife4j 文档异常问题解决方案

## 问题描述

在Spring Boot 3.5.3项目中集成Knife4j 4.5.0时遇到以下异常：

```
java.lang.NoSuchMethodError: 'void org.springframework.web.method.ControllerAdviceBean.<init>(java.lang.Object)'
```

该异常导致API文档无法正常访问，返回500错误。

## 根本原因

这是一个版本兼容性问题：
- **Spring Boot 3.5.3** 使用了较新的Spring Framework版本
- **Knife4j 4.5.0** 尚未完全兼容最新的Spring Boot 3.5.x版本
- 冲突主要出现在`@RestControllerAdvice`注解的处理上，特别是`ControllerAdviceBean`的构造函数签名变更

## 解决方案

### 方案1：临时禁用GlobalExceptionHandler（已采用）

在`GlobalExceptionHandler.java`中注释掉`@RestControllerAdvice`注解：

```java
// @RestControllerAdvice
public class GlobalExceptionHandler {
    // ... 保持方法不变
}
```

**优点：**
- 快速解决问题，Knife4j可以正常使用
- 不需要大幅修改现有代码

**缺点：**
- 失去全局异常处理功能
- 需要在各Controller中单独处理异常

### 方案2：降级Spring Boot版本

将Spring Boot版本从3.5.3降级到3.3.x：

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.5</version>
    <relativePath/>
</parent>
```

### 方案3：使用纯SpringDoc OpenAPI

移除Knife4j依赖，使用原生SpringDoc：

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

### 方案4：等待Knife4j更新

等待Knife4j官方发布兼容Spring Boot 3.5.x的新版本。

## 当前状态

- ✅ Knife4j UI可以正常访问：http://localhost:8080/doc.html
- ✅ OpenAPI文档端点正常：http://localhost:8080/v3/api-docs
- ✅ API接口正常工作（登录、注册、/me接口）
- ❌ 全局异常处理暂时禁用

## 验证步骤

1. 启动应用：`mvn spring-boot:run`
2. 访问Knife4j UI：http://localhost:8080/doc.html
3. 测试API接口功能
4. 检查无异常日志

## 建议

建议采用**方案2（降级Spring Boot）**作为长期解决方案，因为：
1. 保持功能完整性
2. 避免兼容性问题
3. Spring Boot 3.3.x版本已经足够稳定

如果必须使用Spring Boot 3.5.3，则建议采用**方案3（使用SpringDoc）**替代Knife4j。

## 相关配置

已更新的Knife4j配置：
- 版本：4.5.0
- 支持JWT认证
- 中文界面
- 完整的OpenAPI 3.0规范支持
