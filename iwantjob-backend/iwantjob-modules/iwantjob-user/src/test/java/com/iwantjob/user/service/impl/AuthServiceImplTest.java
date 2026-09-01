package com.iwantjob.user.service.impl;

import com.iwantjob.common.enums.UserRoleEnum;
import com.iwantjob.common.exception.BusinessException;
import com.iwantjob.common.result.ErrorCode;
import com.iwantjob.framework.security.JwtUtils;
import com.iwantjob.user.dto.LoginDTO;
import com.iwantjob.user.dto.LoginVO;
import com.iwantjob.user.dto.RegisterDTO;
import com.iwantjob.user.entity.MutualPoints;
import com.iwantjob.user.entity.SysUser;
import com.iwantjob.user.entity.UserProfile;
import com.iwantjob.user.mapper.MutualPointsMapper;
import com.iwantjob.user.mapper.SysUserMapper;
import com.iwantjob.user.mapper.UserProfileMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 认证服务单元测试：注册（重名/邮箱占用/非法角色/成功）、登录（用户不存在/密码错误/禁用/成功）
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private SysUserMapper sysUserMapper;
    @Mock
    private UserProfileMapper userProfileMapper;
    @Mock
    private MutualPointsMapper mutualPointsMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private StringRedisTemplate redisTemplate;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(sysUserMapper, userProfileMapper,
                mutualPointsMapper, passwordEncoder, jwtUtils, redisTemplate);
    }

    private RegisterDTO buildRegisterDTO() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("alice");
        dto.setPassword("Abc123456");
        dto.setEmail("alice@test.com");
        dto.setRole(0);
        return dto;
    }

    // ==================== 注册 ====================

    @Test
    void registerShouldCreateUserWithEncodedPassword() {
        when(sysUserMapper.selectCount(any())).thenReturn(0L);
        when(passwordEncoder.encode("Abc123456")).thenReturn("$2a$10$encoded");
        doAnswer(inv -> {
            ((SysUser) inv.getArgument(0)).setId(100L);
            return 1;
        }).when(sysUserMapper).insert(any(SysUser.class));

        Long userId = authService.register(buildRegisterDTO());

        assertEquals(100L, userId);
        // 验证用户落库：密码已加密、状态正常
        ArgumentCaptor<SysUser> userCaptor = ArgumentCaptor.forClass(SysUser.class);
        verify(sysUserMapper).insert(userCaptor.capture());
        assertEquals("$2a$10$encoded", userCaptor.getValue().getPassword());
        assertEquals("alice", userCaptor.getValue().getUsername());
        assertEquals(1, userCaptor.getValue().getStatus());
        // 验证资料与积分账户初始化
        verify(userProfileMapper).insert(any(UserProfile.class));
        ArgumentCaptor<MutualPoints> pointsCaptor = ArgumentCaptor.forClass(MutualPoints.class);
        verify(mutualPointsMapper).insert(pointsCaptor.capture());
        assertEquals(0, pointsCaptor.getValue().getBalance());
    }

    @Test
    void registerWithExistingUsernameShouldThrowUserExists() {
        when(sysUserMapper.selectCount(any())).thenReturn(1L);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.register(buildRegisterDTO()));
        assertEquals(ErrorCode.USER_EXISTS.getCode(), ex.getCode());
        verify(sysUserMapper, never()).insert(any(SysUser.class));
    }

    @Test
    void registerWithExistingEmailShouldThrowUserExists() {
        // 第一次 selectCount（用户名）返回 0，第二次（邮箱）返回 1
        when(sysUserMapper.selectCount(any())).thenReturn(0L, 1L);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.register(buildRegisterDTO()));
        assertEquals(ErrorCode.USER_EXISTS.getCode(), ex.getCode());
    }

    @Test
    void registerWithIllegalRoleShouldThrowParamError() {
        RegisterDTO dto = buildRegisterDTO();
        dto.setRole(99); // 非法角色码
        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.register(dto));
        assertEquals(ErrorCode.PARAM_ERROR.getCode(), ex.getCode());
    }

    @Test
    void registerShouldAcceptAllDefinedRoles() {
        when(sysUserMapper.selectCount(any())).thenReturn(0L);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encoded");
        for (UserRoleEnum role : UserRoleEnum.values()) {
            RegisterDTO dto = buildRegisterDTO();
            dto.setRole(role.getCode());
            authService.register(dto);
        }
        // 每个合法角色（5种）都能注册成功
        verify(sysUserMapper, org.mockito.Mockito.times(UserRoleEnum.values().length))
                .insert(any(SysUser.class));
    }

    // ==================== 登录 ====================

    private SysUser buildActiveUser() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("alice");
        user.setPassword("$2a$10$encoded");
        user.setRole(0);
        user.setStatus(1);
        return user;
    }

    @Test
    void loginWithCorrectCredentialShouldReturnTokens() {
        SysUser user = buildActiveUser();
        when(sysUserMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches("Abc123456", "$2a$10$encoded")).thenReturn(true);
        when(jwtUtils.generateAccessToken(1L, "alice", 0)).thenReturn("access-token");
        when(jwtUtils.generateRefreshToken(1L, "alice", 0)).thenReturn("refresh-token");
        when(jwtUtils.getAccessExpiration()).thenReturn(7200L);

        LoginDTO dto = new LoginDTO();
        dto.setUsername("alice");
        dto.setPassword("Abc123456");
        LoginVO vo = authService.login(dto);

        assertEquals("access-token", vo.getAccessToken());
        assertEquals("refresh-token", vo.getRefreshToken());
        assertEquals(7200L, vo.getExpiresIn());
        // 登录应更新最近登录时间
        verify(sysUserMapper).updateById(any(SysUser.class));
    }

    @Test
    void loginWithUnknownUserShouldThrowUserNotFound() {
        when(sysUserMapper.selectOne(any())).thenReturn(null);
        LoginDTO dto = new LoginDTO();
        dto.setUsername("ghost");
        dto.setPassword("whatever");
        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.login(dto));
        assertEquals(ErrorCode.USER_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    void loginWithWrongPasswordShouldThrowPasswordError() {
        when(sysUserMapper.selectOne(any())).thenReturn(buildActiveUser());
        when(passwordEncoder.matches("wrong", "$2a$10$encoded")).thenReturn(false);
        LoginDTO dto = new LoginDTO();
        dto.setUsername("alice");
        dto.setPassword("wrong");
        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.login(dto));
        assertEquals(ErrorCode.PASSWORD_ERROR.getCode(), ex.getCode());
    }

    @Test
    void loginWithDisabledUserShouldThrowUserDisabled() {
        SysUser user = buildActiveUser();
        user.setStatus(0);
        when(sysUserMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        LoginDTO dto = new LoginDTO();
        dto.setUsername("alice");
        dto.setPassword("Abc123456");
        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.login(dto));
        assertEquals(ErrorCode.USER_DISABLED.getCode(), ex.getCode());
    }
}
