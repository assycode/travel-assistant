package com.example.demo.service.impl;

import com.example.demo.dto.*;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.TripPlanMapper;
import com.example.demo.mapper.UserScenicCollectMapper;
import com.example.demo.util.AliOssUtil;
import com.example.demo.util.ErrorCode;
import com.example.demo.util.Result;
import com.example.demo.util.TokenUtil;
import com.example.demo.entity.SysUser;
import com.example.demo.mapper.SysUserMapper;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;


@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private TripPlanMapper tripPlanMapper;

    @Autowired
    private UserScenicCollectMapper userScenicCollectMapper;

    @Autowired
    private AliOssUtil aliOssUtil;

    // BCrypt 密码加密器（推荐使用 Spring Security 配置）
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UserRegisterResponse register(UserRegisterRequest req) {
        // 1. 检查用户名是否已存在
        SysUser existUser = sysUserMapper.selectByUsername(req.getUsername());
        if (existUser != null) {
            throw new RuntimeException(ErrorCode.PARAM_ERROR.getMsg());
        }

        // 2. 密码加密
        String encodedPwd = passwordEncoder.encode(req.getPassword());

        // 3. 保存用户到数据库
        SysUser user = new SysUser();
        user.setUsername(req.getUsername());
        user.setPassword(encodedPwd);
        user.setPhone(req.getPhone());
        user.setStatus(1); // 正常状态
        sysUserMapper.insert(user);

        // 4. 构造响应
        UserRegisterResponse resp = new UserRegisterResponse();
        resp.setUserId(user.getId());
        resp.setUserName(user.getUsername());
        return resp;
    }

    @Override
    public UserLoginResponse login(UserLoginRequest req) {
        // 1. 根据用户名查询用户
        SysUser user = sysUserMapper.selectByUsername(req.getUserName());
        if (user == null) {
            throw new RuntimeException(ErrorCode.PARAM_ERROR.getMsg());
        }

        // 2. 验证密码
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException(ErrorCode.PARAM_ERROR.getMsg());
        }

        // 3. 生成 JWT Token
        String token = TokenUtil.generateAccessToken(user.getId().toString(), null);
        System.out.println(token);
        // 4. 构造响应
        UserLoginResponse resp = new UserLoginResponse();
        resp.setToken(token);
        resp.setUserId(user.getId());
        resp.setUserName(user.getUsername());
        return resp;
    }

    /**
     * 3. 获取当前用户信息（含统计）
     */
    @Override
    public Result<UserProfileVO> getProfile(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户未登录");
        }
        // 1. 查询用户基础信息
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户不存在");
        }
        // 2. 查询统计数据
        Integer planCount = tripPlanMapper.countByUserId(userId);
        Integer collectionCount = userScenicCollectMapper.countByUserId(userId);
        // 3. 封装返回
        UserProfileVO profile = new UserProfileVO();
        profile.setName(user.getNickname());
        profile.setPhone(user.getPhone());
        profile.setAvatar(user.getAvatar());
        profile.setPlanCount(planCount);
        profile.setCollectionCount(collectionCount);
        return Result.success(profile);
    }

    /**
     * 4. 修改昵称
     */
    @Override
    public Result<Void> updateNickname(Long userId, String newName) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户未登录");
        }
        if (!StringUtils.hasText(newName)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "新昵称不能为空");
        }
        SysUser user = new SysUser();
        user.setId(userId);
        user.setNickname(newName);
        user.setUpdateTime(new Date());
        sysUserMapper.updateById(user);
        return Result.success(null, "昵称修改成功");
    }

    /**
     * 5. 修改密码
     */
    @Override
    public Result<Void> updatePassword(Long userId, String oldPwd, String newPwd) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户未登录");
        }
        if (!StringUtils.hasText(oldPwd) || !StringUtils.hasText(newPwd)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "旧密码和新密码不能为空");
        }
        // 1. 查询原用户密码
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户不存在");
        }
        // 2. 校验旧密码（BCrypt 验证）
        if (!passwordEncoder.matches(oldPwd, user.getPassword())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "旧密码错误");
        }
        // 3. 加密新密码并更新
        String encodedNewPwd = passwordEncoder.encode(newPwd);
        SysUser updateUser = new SysUser();
        updateUser.setId(userId);
        updateUser.setPassword(encodedNewPwd);
        updateUser.setUpdateTime(new Date());
        sysUserMapper.updateById(updateUser);
        return Result.success(null, "密码修改成功");
    }

    /**
     * 6. 更换头像（上传到 OSS）
     */
    @Override
    public Result<String> updateAvatar(Long userId, MultipartFile file) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户未登录");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "头像文件不能为空");
        }
        try {
            // 1. 读取文件字节流
            byte[] fileBytes = file.getBytes();
            // 2. 上传到 OSS（复用 AliOssUtil，需补充 MultipartFile 重载方法）
            String ossUrl = aliOssUtil.uploadAvatar(file);
            if (ossUrl == null) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "头像上传 OSS 失败");
            }
            // 3. 更新数据库头像路径
            SysUser user = new SysUser();
            user.setId(userId);
            user.setAvatar(ossUrl);
            user.setUpdateTime(new Date());
            sysUserMapper.updateById(user);
            return Result.success(ossUrl, "头像更换成功");
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "头像上传失败：" + e.getMessage());
        }
    }
}
