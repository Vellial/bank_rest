package com.example.bankcards.service;

import com.example.bankcards.dto.user.AuthResponse;
import com.example.bankcards.dto.user.LoginRequest;
import com.example.bankcards.dto.user.RefreshRequest;
import com.example.bankcards.dto.user.RegisterRequest;
import com.example.bankcards.entity.BankUser;
import com.example.bankcards.entity.RoleType;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.repository.BankUserRepository;
import com.example.bankcards.repository.UserRoleRepository;
import com.example.bankcards.security.JWTTokenProvider;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.text.ParseException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AuthServiceTest {

    @Mock
    private BankUserRepository bankUserRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JWTTokenProvider jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private final String username = "testuser";
    private final String password = "password123";
    private final String token = "fake-jwt-token";
    private final String refreshToken = "fake-refresh-token";
    private final BankUser testUser = new BankUser();
    private final UserRole testRole = new UserRole();

    @BeforeEach
    void setUp() throws JOSEException, BadJOSEException, ParseException {
        testRole.setRoleType(RoleType.USER);
        testUser.setUsername(username);
        testUser.setPassword(password);
        testUser.setUserRole(testRole);

        when(jwtService.generateToken(any(UserDetails.class))).thenReturn(token);
        when(jwtService.generateRefreshToken(any(UserDetails.class))).thenReturn(refreshToken);
        when(jwtService.getSubject(refreshToken)).thenReturn(username);

        Authentication auth = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(username);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
    }

    @Test
    void register_shouldCreateUserWithRole_whenRoleDoesNotExist() throws JOSEException {
        when(bankUserRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(userRoleRepository.findByRoleType(RoleType.USER)).thenReturn(Optional.empty());
        when(userRoleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(passwordEncoder.encode(password)).thenReturn("encoded-password");
        when(bankUserRepository.save(any())).thenReturn(testUser);

        AuthResponse result = authService.register(new RegisterRequest(username, password, "test@example.com", "Test User"));

        assertEquals(token, result.token());
        assertEquals(refreshToken, result.refreshToken());
        assertEquals(username, result.username());
        assertEquals(RoleType.USER.name(), result.role());
        verify(userRoleRepository).save(any());
        verify(bankUserRepository).save(any());
    }

    @Test
    void register_shouldUseExistingRole_whenRoleExists() throws JOSEException {
        when(bankUserRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(userRoleRepository.findByRoleType(RoleType.USER)).thenReturn(Optional.of(testRole));
        when(passwordEncoder.encode(password)).thenReturn("encoded-password");
        when(bankUserRepository.save(any())).thenReturn(testUser);

        AuthResponse result = authService.register(new RegisterRequest(username, password, "test@example.com", "Test User"));

        assertEquals(RoleType.USER.name(), result.role());
        verify(userRoleRepository, never()).save(any());
        verify(bankUserRepository).save(any());
    }

    @Test
    void login_shouldAuthenticateAndReturnTokens() throws JOSEException {
        when(bankUserRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        AuthResponse result = authService.login(new LoginRequest(username, password));

        assertEquals(token, result.token());
        assertEquals(refreshToken, result.refreshToken());
        assertEquals(username, result.username());
        assertEquals(RoleType.USER.name(), result.role());
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void login_shouldThrowAuthenticationServiceException_whenAuthenticationFails() {
        when(authenticationManager.authenticate(any())).thenThrow(new RuntimeException());

        assertThrows(AuthenticationServiceException.class, () ->
                authService.login(new LoginRequest(username, "wrong"))
        );
    }

    @Test
    void login_shouldThrowAuthenticationServiceException_whenUserNotFoundAfterAuth() {
        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
        when(bankUserRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(AuthenticationServiceException.class, () ->
                authService.login(new LoginRequest(username, password))
        );
    }

    @Test
    void refresh_shouldReturnTokens_whenRefreshTokenValid() throws BadJOSEException, ParseException, JOSEException {
        when(jwtService.getSubject(refreshToken)).thenReturn(username);
        when(bankUserRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        AuthResponse result = authService.refresh(new RefreshRequest(refreshToken));

        assertEquals(token, result.token());
        assertEquals(refreshToken, result.refreshToken());
        assertEquals(username, result.username());
        assertEquals(RoleType.USER.name(), result.role());
    }

    @Test
    void refresh_shouldThrowAuthenticationServiceException_whenRefreshTokenInvalid() throws BadJOSEException, ParseException, JOSEException {
        when(jwtService.getSubject(anyString())).thenThrow(new JOSEException());

        assertThrows(AuthenticationServiceException.class, () ->
                authService.refresh(new RefreshRequest("invalid"))
        );
    }

    @Test
    void refresh_shouldThrowAuthenticationServiceException_whenUserNotFoundByRefreshToken() throws BadJOSEException, ParseException, JOSEException {
        when(jwtService.getSubject(refreshToken)).thenReturn("nonexistent");
        when(bankUserRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(AuthenticationServiceException.class, () ->
                authService.refresh(new RefreshRequest(refreshToken))
        );
    }

    @Test
    void toResponse_shouldGenerateTokensAndSetAuthentication() throws JOSEException {
        AuthResponse result = authService.toResponse(testUser);

        assertEquals(token, result.token());
        assertEquals(refreshToken, result.refreshToken());
        assertEquals(username, result.username());
        assertEquals(RoleType.USER.name(), result.role());
        verify(authenticationManager).authenticate(any());
    }
}
