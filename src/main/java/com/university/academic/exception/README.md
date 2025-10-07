# 统一响应和异常处理使用指南

## 概述

本系统实现了统一的响应格式和全局异常处理机制，确保API返回的数据格式一致，异常处理规范。

## 统一响应格式

所有API接口都应返回 `Result<T>` 类型：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... },
  "timestamp": "2024-10-07T10:30:00"
}
```

### Result 类使用示例

```java
@RestController
@RequestMapping("/api/students")
public class StudentController {
    
    // 成功响应（无数据）
    @DeleteMapping("/{id}")
    public Result<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return Result.success();
    }
    
    // 成功响应（带消息）
    @PostMapping
    public Result<Void> addStudent(@RequestBody StudentDTO dto) {
        studentService.addStudent(dto);
        return Result.success("学生添加成功");
    }
    
    // 成功响应（带数据）
    @GetMapping("/{id}")
    public Result<StudentVO> getStudent(@PathVariable Long id) {
        StudentVO student = studentService.getStudent(id);
        return Result.success(student);
    }
    
    // 成功响应（带消息和数据）
    @PutMapping("/{id}")
    public Result<StudentVO> updateStudent(@PathVariable Long id, @RequestBody StudentDTO dto) {
        StudentVO student = studentService.updateStudent(id, dto);
        return Result.success("学生信息更新成功", student);
    }
    
    // 失败响应（通常在Service层抛出异常，由全局异常处理器处理）
}
```

## 异常处理

### 1. 业务异常 (BusinessException)

用于处理业务逻辑中的异常情况。

```java
@Service
public class StudentService {
    
    public StudentVO getStudent(Long id) {
        Student student = studentRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
        return convertToVO(student);
    }
    
    public void addStudent(StudentDTO dto) {
        // 检查学号是否已存在
        if (studentRepository.existsByStudentNo(dto.getStudentNo())) {
            throw new BusinessException(ErrorCode.STUDENT_NO_ALREADY_EXISTS);
        }
        // ... 添加学生逻辑
    }
    
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
            
        // 检查是否有在读课程
        if (hasActiveCourses(student)) {
            throw new BusinessException(ErrorCode.STUDENT_HAS_COURSES);
        }
        
        // 软删除
        student.setDeleted(true);
        studentRepository.save(student);
    }
}
```

### 2. 参数验证异常 (ValidationException)

用于处理参数校验失败的情况。

```java
@Service
public class GradeService {
    
    public void saveGrade(GradeDTO dto) {
        // 验证成绩范围
        if (dto.getRegularScore() < 0 || dto.getRegularScore() > 100) {
            throw new ValidationException("平时成绩必须在0-100之间");
        }
        
        if (dto.getMidtermScore() < 0 || dto.getMidtermScore() > 100) {
            throw new ValidationException(ErrorCode.GRADE_SCORE_INVALID);
        }
        
        // ... 保存成绩逻辑
    }
}
```

### 3. 使用 Bean Validation

推荐使用 Bean Validation 注解进行参数校验，全局异常处理器会自动处理。

```java
@Data
public class StudentDTO {
    
    @NotBlank(message = "学号不能为空")
    @Size(max = 20, message = "学号长度不能超过20个字符")
    private String studentNo;
    
    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过50个字符")
    private String name;
    
    @NotNull(message = "性别不能为空")
    private Student.Gender gender;
    
    @Past(message = "出生日期必须是过去的日期")
    private LocalDate birthDate;
    
    @NotNull(message = "入学年份不能为空")
    @Min(value = 2000, message = "入学年份不能早于2000年")
    private Integer enrollmentYear;
}

@RestController
@RequestMapping("/api/students")
public class StudentController {
    
    @PostMapping
    public Result<Void> addStudent(@Valid @RequestBody StudentDTO dto) {
        // @Valid 注解会自动触发校验，如果失败会被全局异常处理器捕获
        studentService.addStudent(dto);
        return Result.success("学生添加成功");
    }
}
```

### 4. 未授权异常 (UnauthorizedException)

用于处理未登录或token无效的情况。

```java
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
        String token = getTokenFromRequest(request);
        
        if (token == null) {
            throw new UnauthorizedException("未提供认证令牌");
        }
        
        if (!jwtUtil.validateToken(token)) {
            throw new UnauthorizedException(ErrorCode.TOKEN_INVALID);
        }
        
        // ... 设置认证信息
        chain.doFilter(request, response);
    }
}
```

### 5. 权限不足异常 (ForbiddenException)

用于处理权限不足的情况。

```java
@Service
public class CourseService {
    
    public void updateCourse(Long courseId, CourseDTO dto, User currentUser) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));
        
        // 检查权限（只有管理员可以修改）
        if (currentUser.getRole() != User.UserRole.ADMIN) {
            throw new ForbiddenException("只有管理员可以修改课程信息");
        }
        
        // ... 更新课程逻辑
    }
}
```

## 错误码定义

系统定义了完整的错误码枚举 `ErrorCode`，按模块分类：

| 错误码范围 | 模块 | 说明 |
|-----------|------|------|
| 1000-1999 | 通用错误 | 系统异常、参数错误、未授权等 |
| 2000-2099 | 认证相关 | 登录失败、账户锁定、令牌过期等 |
| 3000-3099 | 学生管理 | 学生不存在、学号已存在等 |
| 3100-3199 | 教师管理 | 教师不存在、工号已存在等 |
| 3200-3299 | 课程管理 | 课程不存在、先修课程等 |
| 3300-3399 | 院系专业 | 院系不存在、专业代码已存在等 |
| 3400-3499 | 学期管理 | 学期不存在、学期已存在等 |
| 3500-3599 | 开课计划 | 时间冲突、容量错误等 |
| 3600-3699 | 选课管理 | 选课已满、时间冲突、学分超限等 |
| 3700-3799 | 成绩管理 | 成绩无效、已提交等 |
| 3800-3899 | 通知管理 | 通知不存在等 |
| 3900-3999 | 系统配置 | 配置不存在等 |
| 4000-4099 | 文件操作 | 文件上传失败、类型不支持等 |
| 4100-4199 | 数据操作 | 数据不存在、乐观锁失败等 |

### 添加新的错误码

在 `ErrorCode` 枚举中添加：

```java
public enum ErrorCode {
    // ... 现有错误码
    
    // 新增错误码
    NEW_BUSINESS_ERROR(3XXX, "错误描述"),
    
    // ...
}
```

## 全局异常处理器

`GlobalExceptionHandler` 会自动处理以下异常：

- **BusinessException** - 业务异常
- **ValidationException** - 参数验证异常
- **MethodArgumentNotValidException** - Bean Validation异常
- **BindException** - 参数绑定异常
- **MissingServletRequestParameterException** - 缺少请求参数
- **HttpRequestMethodNotSupportedException** - 请求方法不支持
- **NoHandlerFoundException** - 404异常
- **BadCredentialsException** - 认证失败
- **AccessDeniedException** - 权限不足
- **Exception** - 通用异常

所有异常都会被转换为统一的 `Result` 格式返回给前端。

## 最佳实践

1. **Service层抛异常，Controller层不需要捕获**
   ```java
   // ✅ 推荐
   @Service
   public class StudentService {
       public void deleteStudent(Long id) {
           if (!exists(id)) {
               throw new BusinessException(ErrorCode.STUDENT_NOT_FOUND);
           }
           // ... 删除逻辑
       }
   }
   
   @RestController
   public class StudentController {
       @DeleteMapping("/{id}")
       public Result<Void> deleteStudent(@PathVariable Long id) {
           studentService.deleteStudent(id);
           return Result.success("删除成功");
       }
   }
   ```

2. **使用错误码枚举而不是硬编码**
   ```java
   // ❌ 不推荐
   throw new BusinessException(3001, "学生不存在");
   
   // ✅ 推荐
   throw new BusinessException(ErrorCode.STUDENT_NOT_FOUND);
   ```

3. **使用 Bean Validation 进行参数校验**
   ```java
   // ✅ 推荐在DTO上使用注解
   @Data
   public class StudentDTO {
       @NotBlank(message = "学号不能为空")
       private String studentNo;
   }
   
   // Controller中使用@Valid触发校验
   @PostMapping
   public Result<Void> addStudent(@Valid @RequestBody StudentDTO dto) {
       // ...
   }
   ```

4. **统一返回格式**
   ```java
   // ✅ 所有接口都返回Result类型
   public Result<StudentVO> getStudent(Long id) {
       return Result.success(student);
   }
   ```

## 日志记录

全局异常处理器会自动记录异常日志：
- **业务异常** - WARN级别
- **参数异常** - WARN级别  
- **系统异常** - ERROR级别

可以在 `application.yml` 中配置日志级别：

```yaml
logging:
  level:
    com.university.academic: DEBUG
```

