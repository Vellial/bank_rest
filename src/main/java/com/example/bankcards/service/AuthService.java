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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final BankUserRepository bankUserRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTTokenProvider jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) throws JOSEException {
        if (bankUserRepository.findByUsername(request.username()).isPresent()) {
            throw new AuthenticationServiceException("User already exists");
        }

        UserRole userRole = userRoleRepository.findByRoleType(RoleType.USER)
                .orElseGet(() -> {
                    UserRole role = new UserRole();
                    role.setRoleType(RoleType.USER);
                    return userRoleRepository.save(role);
                });

        BankUser user = new BankUser();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setUserRole(userRole);
        user = bankUserRepository.save(user);
        return toResponse(user);
    }

    public AuthResponse login(LoginRequest request) throws JOSEException {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (Exception e) {
            throw new AuthenticationServiceException("Invalid credentials");
        }

        BankUser user = bankUserRepository.findByUsername(request.username())
                .orElseThrow(() -> new AuthenticationServiceException("User not found"));

        return toResponse(user, authentication);
    }

    public AuthResponse refresh(@Valid RefreshRequest request) throws BadJOSEException {
        try {
            String username = jwtService.getSubject(request.refreshToken());
            if (username == null) {
                throw new AuthenticationServiceException("Invalid or expired refresh token");
            }

            BankUser user = bankUserRepository.findByUsername(username)
                    .orElseThrow(() -> new AuthenticationServiceException("Invalid refresh token"));

            return toResponse(user);
        } catch (JOSEException | ParseException e) {
            throw new AuthenticationServiceException("Invalid or expired refresh token");
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

    public AuthResponse toResponse(BankUser user, Authentication authentication) throws JOSEException {
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtService.generateToken((UserDetails) authentication.getPrincipal());
        String refreshToken = jwtService.generateRefreshToken((UserDetails) authentication.getPrincipal());
        return new AuthResponse(token, refreshToken, user.getUsername(), user.getUserRole().getRoleType().name());
    }
}
