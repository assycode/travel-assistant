package com.example.demo;

import com.example.demo.dto.UserLoginRequest;
import com.example.demo.dto.UserLoginResponse;
import com.example.demo.dto.UserRegisterRequest;
import com.example.demo.dto.UserRegisterResponse;
import com.example.demo.service.UserService;
import com.example.demo.util.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 用户登录&注册接口单元测试
 */
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ✅ 关键修复：@MockBean 替代 @Mock
    @MockitoBean
    private UserService userService;

    private UserRegisterRequest validRegisterDTO;
    private UserLoginRequest validLoginDTO;

    @BeforeEach
    void setUp() {
        validRegisterDTO = new UserRegisterRequest();
        validRegisterDTO.setUsername("test_user");
        validRegisterDTO.setPassword("123456");
        validRegisterDTO.setPhone("18360168288");

        validLoginDTO = new UserLoginRequest();
        validLoginDTO.setUserName("test_user");
        validLoginDTO.setPassword("123456");
    }

    // 其余测试方法代码不变，仅保留 CSRF Token 相关的 .with(csrf()) 即可
    @Test
    void testRegister_Success() throws Exception {
        // 模拟 Service 返回结果
        UserRegisterResponse registerVO = new UserRegisterResponse();
        registerVO.setUserId(1L);
        registerVO.setUserName("test_user");
        when(userService.register(any(UserRegisterRequest.class))).thenReturn(registerVO);

        // 发送请求（已带 CSRF Token）
        mockMvc.perform(MockMvcRequestBuilders.post("/user/register")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()) // 保留 CSRF
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterDTO))
                        .header("Accept", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.SUCCESS.getCode()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.userId").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("注册成功"));

        verify(userService, times(1)).register(eq(validRegisterDTO));
    }
    /**
     * 测试场景2：用户名为空，注册失败（参数校验）
     */
    @Test
    void testRegister_UsernameEmpty_Fail() throws Exception {
        // 1. 构造非法参数：用户名为空
        UserRegisterRequest invalidDTO = new UserRegisterRequest();
        invalidDTO.setUsername(""); // 空用户名
        invalidDTO.setPassword("123456");
        invalidDTO.setPhone("18360168288");

        // 2. 发送 POST 请求
        mockMvc.perform(MockMvcRequestBuilders.post("/user/register")
                        // 添加 CSRF Token（核心改动）
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                // 3. 验证响应
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false)) // 请求失败
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.PARAM_ERROR.getCode())) // 错误码 00001
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("用户名不能为空")); // 提示信息

        // 4. 验证 Service 方法未被调用（参数校验失败，不会进入 Service）
        verify(userService, Mockito.never()).register(Mockito.any(UserRegisterRequest.class));
    }

    /**
     * 测试场景3：用户名已存在，注册失败
     */
    @Test
    void testRegister_UsernameExist_Fail() throws Exception {
        // 1. 模拟 Service 抛出“用户名已存在”异常
        when(userService.register(Mockito.any(UserRegisterRequest.class)))
                .thenThrow(new RuntimeException("用户名已存在"));

        // 2. 发送 POST 请求
        mockMvc.perform(MockMvcRequestBuilders.post("/user/register")
                        // 添加 CSRF Token（核心改动）
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterDTO)))
                // 3. 验证响应
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.PARAM_ERROR.getCode()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("用户名已存在"));

        // 4. 验证 Service 方法被调用一次
        verify(userService, times(1)).register(eq(validRegisterDTO));
    }

    // ==================== 登录接口测试 ====================

    /**
     * 测试场景1：登录参数合法，登录成功（返回 Token）
     */
    @Test
    void testLogin_Success() throws Exception {
        // 1. 模拟 Service 返回登录结果（含 Token）
        UserLoginResponse loginVO = new UserLoginResponse();
        loginVO.setUserId(1L);
        loginVO.setUserName("test_user");
        loginVO.setToken("mock_jwt_token_123456"); // 模拟生成的 Token
        when(userService.login(Mockito.any(UserLoginRequest.class))).thenReturn(loginVO);

        // 2. 发送 POST 请求测试登录接口
        mockMvc.perform(MockMvcRequestBuilders.post("/user/login")
                        // 添加 CSRF Token（核心改动）
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginDTO)))
                // 3. 验证响应
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.SUCCESS.getCode()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.token").value("mock_jwt_token_123456")) // 返回 Token
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.userId").value(1L)) // 返回用户ID
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("登录成功"));

        // 4. 验证 Service 方法被调用一次
        verify(userService, times(1)).login(eq(validLoginDTO));
    }

    /**
     * 测试场景2：密码错误，登录失败
     */
    @Test
    void testLogin_PasswordError_Fail() throws Exception {
        // 1. 构造错误密码的登录参数
        UserLoginRequest invalidLoginDTO = new UserLoginRequest();
        invalidLoginDTO.setUserName("test_user");
        invalidLoginDTO.setPassword("wrong_password");

        // 2. 模拟 Service 抛出“密码错误”异常
        when(userService.login(eq(invalidLoginDTO)))
                .thenThrow(new RuntimeException("密码错误"));

        // 3. 发送 POST 请求
        mockMvc.perform(MockMvcRequestBuilders.post("/user/login")
                        // 添加 CSRF Token（核心改动）
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLoginDTO)))
                // 4. 验证响应
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.PARAM_ERROR.getCode()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("密码错误"));

        // 5. 验证 Service 方法被调用一次
        verify(userService, times(1)).login(eq(invalidLoginDTO));
    }

    /**
     * 测试场景3：用户名不存在，登录失败
     */
    @Test
    void testLogin_UsernameNotExist_Fail() throws Exception {
        // 1. 构造不存在的用户名
        UserLoginRequest invalidLoginDTO = new UserLoginRequest();
        invalidLoginDTO.setUserName("not_exist_user");
        invalidLoginDTO.setPassword("123456");

        // 2. 模拟 Service 抛出“用户名不存在”异常
        when(userService.login(eq(invalidLoginDTO)))
                .thenThrow(new RuntimeException("用户名不存在"));

        // 3. 发送 POST 请求
        mockMvc.perform(MockMvcRequestBuilders.post("/user/login")
                        // 添加 CSRF Token（核心改动）
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLoginDTO)))
                // 4. 验证响应
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.PARAM_ERROR.getCode()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("用户名不存在"));
    }
}