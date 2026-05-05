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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountException;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final BankUserRepository bankUserRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTTokenProvider jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) throws AccountException, JOSEException {
        BankUser user = bankUserRepository.findByUsername(request.username()).orElseThrow(
                () -> new AccountException("User not found")
        );
        userRoleRepository.findByRoleType(RoleType.USER)
                .ifPresentOrElse(user::setUserRole,
                        () -> {
                            UserRole userRole = new UserRole();
                            userRole.setRoleType(RoleType.USER);
                            user.setUserRole(userRole);
                            userRoleRepository.save(userRole);
                        }
                );
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return toResponse(bankUserRepository.save(user));
    }

    public AuthResponse login(LoginRequest request) throws AccountException, JOSEException {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        BankUser user = bankUserRepository.findByUsername(request.username())
                .orElseThrow(() -> new AccountException("User not found"));

        return toResponse(user);
    }

    public AuthResponse refresh(@Valid RefreshRequest request) throws AccountException {
        try {
            String username = jwtService.getSubject(request.refreshToken());
            BankUser user = bankUserRepository.findByUsername(username)
                    .orElseThrow(() -> new AccountException("Invalid refresh token"));

            return toResponse(user);
        } catch (Exception e) {
            throw new AccountException("Invalid or expired refresh token");
        }
    }

    public AuthResponse toResponse(BankUser user) throws JOSEException {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtService.generateToken((UserDetails) authentication.getPrincipal());
        String refreshToken = jwtService.generateRefreshToken((UserDetails) authentication.getPrincipal());
        return new AuthResponse(token, refreshToken, user.getUsername(), user.getUserRole().getRoleType().name());
    }
}
