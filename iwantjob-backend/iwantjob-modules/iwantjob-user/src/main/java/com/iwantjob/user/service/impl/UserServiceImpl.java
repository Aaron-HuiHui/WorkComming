package com.iwantjob.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.iwantjob.common.exception.BusinessException;
import com.iwantjob.common.result.ErrorCode;
import com.iwantjob.user.dto.ProfileUpdateDTO;
import com.iwantjob.user.dto.UserInfoVO;
import com.iwantjob.user.entity.SysUser;
import com.iwantjob.user.entity.UserProfile;
import com.iwantjob.user.mapper.SysUserMapper;
import com.iwantjob.user.mapper.UserProfileMapper;
import com.iwantjob.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper sysUserMapper;
    private final UserProfileMapper userProfileMapper;

    @Override
    public SysUser getById(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    @Override
    public UserInfoVO getCurrentUserInfo(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        UserProfile profile = userProfileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, userId));

        UserInfoVO vo = new UserInfoVO();
        // 拷贝 user 字段
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setRole(user.getRole());
        vo.setRealName(user.getRealName());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setStatus(user.getStatus());
        vo.setLastLogin(user.getLastLogin());

        // 拷贝 profile 字段（可能为空）
        if (profile != null) {
            vo.setSchool(profile.getSchool());
            vo.setMajor(profile.getMajor());
            vo.setGraduationYear(profile.getGraduationYear());
            vo.setSkills(profile.getSkills());
            vo.setBio(profile.getBio());
            vo.setAvailableStatus(profile.getAvailableStatus());
            vo.setResumeId(profile.getResumeId());
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long userId, ProfileUpdateDTO dto) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 1. 更新 sys_user 上的 realName / avatarUrl（如有传）
        boolean userChanged = false;
        if (dto.getRealName() != null) {
            user.setRealName(dto.getRealName());
            userChanged = true;
        }
        if (dto.getAvatarUrl() != null) {
            user.setAvatarUrl(dto.getAvatarUrl());
            userChanged = true;
        }
        if (userChanged) {
            sysUserMapper.updateById(user);
        }

        // 2. 查询或创建 profile
        UserProfile profile = userProfileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, userId));
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(userId);
            profile.setAvailableStatus(1);
            userProfileMapper.insert(profile);
        }

        // 3. 选择性更新 profile 字段（null 跳过）
        if (dto.getSchool() != null) profile.setSchool(dto.getSchool());
        if (dto.getMajor() != null) profile.setMajor(dto.getMajor());
        if (dto.getGraduationYear() != null) profile.setGraduationYear(dto.getGraduationYear());
        if (dto.getSkills() != null) profile.setSkills(dto.getSkills());
        if (dto.getBio() != null) profile.setBio(dto.getBio());
        if (dto.getAvailableStatus() != null) profile.setAvailableStatus(dto.getAvailableStatus());

        userProfileMapper.updateById(profile);
        log.info("更新用户资料: userId={}", userId);
    }
}
