package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.util.ErrorCode;
import com.example.demo.util.Result;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 用户注册接口
     */
    @PostMapping("/register")
    public Result<UserRegisterResponse> register(@RequestBody UserRegisterRequest req) {
        // 参数校验
        if (req.getUsername() == null || req.getUsername().isBlank()) {
            return Result.fail(ErrorCode.PARAM_ERROR, "用户名不能为空");
        }
        if (req.getPassword() == null || req.getPassword().isBlank()) {
            return Result.fail(ErrorCode.PARAM_ERROR, "密码不能为空");
        }
        if (req.getPhone() == null || req.getPhone().isBlank()) {
            return Result.fail(ErrorCode.PARAM_ERROR, "手机号不能为空");
        }

        UserRegisterResponse resp = userService.register(req);
        return Result.success(resp, "注册成功");
    }

    /**
     * 用户登录接口
     */
    @PostMapping("/login")
    public Result<UserLoginResponse> login(@RequestBody UserLoginRequest req) {
        // 参数校验
        if (req.getUserName() == null || req.getUserName().isBlank()) {
            return Result.fail(ErrorCode.PARAM_ERROR, "用户名不能为空");
        }
        if (req.getPassword() == null || req.getPassword().isBlank()) {
            return Result.fail(ErrorCode.PARAM_ERROR, "密码不能为空");
        }

        UserLoginResponse resp = userService.login(req);
        return Result.success(resp, "登录成功");
    }
    /**
     * 3. 获取当前用户信息（及统计）
     */
    @GetMapping("/profile")
    public Result<UserProfileVO> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.getProfile(userId);
    }

    /**
     * 4. 修改昵称
     */
    @PutMapping("/name")
    public Result<Void> updateName(HttpServletRequest request, @RequestBody UserNameUpdateRequest requestBody) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.updateNickname(userId, requestBody.getNewName());
    }

    /**
     * 5. 修改密码
     */
    @PutMapping("/password")
    public Result<Void> updatePassword(HttpServletRequest request, @RequestBody UserPasswordUpdateRequest requestBody) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.updatePassword(userId, requestBody.getOldPwd(), requestBody.getNewPwd());
    }

    /**
     * 6. 更换头像
     */
    @PostMapping("/avatar")
    public Result<String> updateAvatar(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.updateAvatar(userId, file);
    }
}
