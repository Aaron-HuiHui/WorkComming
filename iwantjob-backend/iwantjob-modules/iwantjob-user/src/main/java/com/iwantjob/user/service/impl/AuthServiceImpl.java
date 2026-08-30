package com.iwantjob.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.iwantjob.common.enums.UserRoleEnum;
import com.iwantjob.common.exception.BusinessException;
import com.iwantjob.common.result.ErrorCode;
import com.iwantjob.framework.security.JwtUtils;
import com.iwantjob.user.dto.LoginDTO;
import com.iwantjob.user.dto.LoginVO;
import com.iwantjob.user.dto.RefreshDTO;
import com.iwantjob.user.dto.RegisterDTO;
import com.iwantjob.user.entity.MutualPoints;
import com.iwantjob.user.entity.SysUser;
import com.iwantjob.user.entity.UserProfile;
import com.iwantjob.user.mapper.MutualPointsMapper;
import com.iwantjob.user.mapper.SysUserMapper;
import com.iwantjob.user.mapper.UserProfileMapper;
import com.iwantjob.user.service.AuthService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final UserProfileMapper userProfileMapper;
    private final MutualPointsMapper mutualPointsMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final StringRedisTemplate redisTemplate;

    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(RegisterDTO dto) {
        // 1. 校验角色合法性
        try {
            UserRoleEnum.of(dto.getRole());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "非法角色码: " + dto.getRole());
        }

        // 2. 用户名唯一性校验
        Long usernameCount = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()));
        if (usernameCount != null && usernameCount > 0) {
            throw new BusinessException(ErrorCode.USER_EXISTS);
        }

        // 3. 邮箱唯一性校验
        Long emailCount = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, dto.getEmail()));
        if (emailCount != null && emailCount > 0) {
            throw new BusinessException(ErrorCode.USER_EXISTS, "邮箱已被注册");
        }

        // 4. 创建用户
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        user.setStatus(1);
        sysUserMapper.insert(user);

        // 5. 初始化用户资料
        UserProfile profile = new UserProfile();
        profile.setUserId(user.getId());
        profile.setAvailableStatus(1);
        userProfileMapper.insert(profile);

        // 6. 初始化积分账户（余额0）
        MutualPoints points = new MutualPoints();
        points.setUserId(user.getId());
        points.setBalance(0);
        points.setTotalEarned(0);
        points.setVersion(0);
        mutualPointsMapper.insert(points);

        log.info("用户注册成功: userId={}, username={}, role={}", user.getId(), user.getUsername(), user.getRole());
        return user.getId();
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        // 1. 查询用户
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()));
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 2. 校验密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        // 3. 校验状态
        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }

        // 4. 更新最近登录时间
        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setLastLogin(LocalDateTime.now());
        sysUserMapper.updateById(update);

        // 5. 生成token
        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getUsername(), user.getRole());

        log.info("用户登录成功: userId={}, username={}", user.getId(), user.getUsername());
        return new LoginVO(accessToken, refreshToken, jwtUtils.getAccessExpiration());
    }

    @Override
    public LoginVO refresh(RefreshDTO dto) {
        String refreshToken = dto.getRefreshToken();
        // 1. 校验token格式与签名
        if (!jwtUtils.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }

        // 2. 校验token类型必须是 REFRESH
        Claims claims;
        try {
            claims = jwtUtils.parseToken(refreshToken);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
        if (!"REFRESH".equals(claims.get("type", String.class))) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID, "非刷新令牌");
        }

        // 3. 校验是否在黑名单
        if (Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + refreshToken))) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID, "令牌已失效");
        }

        // 4. 解析用户信息并签发新token
        Long userId = claims.get("userId", Long.class);
        String username = claims.getSubject();
        Integer role = claims.get("role", Integer.class);

        // 5. 校验用户当前状态（防止被封禁用户继续刷新）
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }

        String newAccessToken = jwtUtils.generateAccessToken(userId, username, role);
        String newRefreshToken = jwtUtils.generateRefreshToken(userId, username, role);

        // 6. 旧 refreshToken 入黑名单，防止重复使用
        long ttl = computeTokenTtlSeconds(refreshToken);
        if (ttl > 0) {
            redisTemplate.opsForValue().set(BLACKLIST_PREFIX + refreshToken, "1", Duration.ofSeconds(ttl));
        }

        return new LoginVO(newAccessToken, newRefreshToken, jwtUtils.getAccessExpiration());
    }

    @Override
    public void logout(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return;
        }
        // 1. 校验token有效性，无效则不处理（避免攻击者用任意串消耗Redis）
        if (!jwtUtils.validateToken(accessToken)) {
            return;
        }
        // 2. 计算剩余有效期作为TTL
        long ttl = computeTokenTtlSeconds(accessToken);
        if (ttl <= 0) {
            return;
        }
        // 3. 入黑名单
        redisTemplate.opsForValue().set(BLACKLIST_PREFIX + accessToken, "1", Duration.ofSeconds(ttl));
        log.info("用户登出，token已加入黑名单");
    }

    /**
     * 计算token剩余有效期（秒）
     */
    private long computeTokenTtlSeconds(String token) {
        try {
            Claims claims = jwtUtils.parseToken(token);
            long expMillis = claims.getExpiration().getTime();
            long now = System.currentTimeMillis();
            long ttl = (expMillis - now) / 1000;
            return Math.max(ttl, 0);
        } catch (Exception e) {
            return 0L;
        }
    }
}
