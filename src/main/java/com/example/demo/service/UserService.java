package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.util.Result;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Service
public interface UserService {
    UserRegisterResponse register(UserRegisterRequest req);

    UserLoginResponse login(UserLoginRequest req);

    /**
     * 3. 获取当前用户信息（含统计）
     */
    Result<UserProfileVO> getProfile(Long userId);

    /**
     * 4. 修改昵称
     */
    Result<Void> updateNickname(Long userId, String newName);

    /**
     * 5. 修改密码
     */
    Result<Void> updatePassword(Long userId, String oldPwd, String newPwd);

    /**
     * 6. 更换头像（上传到 OSS）
     */
    Result<String> updateAvatar(Long userId, MultipartFile file);
}
