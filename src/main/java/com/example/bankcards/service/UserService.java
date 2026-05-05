package com.example.bankcards.service;

import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.dto.user.UserUpdateRequest;
import com.example.bankcards.entity.BankUser;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.BankUserRepository;
import com.example.bankcards.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.management.relation.RoleNotFoundException;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final BankUserRepository bankUserRepository;
    private final UserRoleRepository userRoleRepository;

    public Page<UserResponse> getAll(Pageable pageable) {
        Page<BankUser> allUsers = bankUserRepository.findAll(pageable);
        return allUsers.map(this::toResponse);
    }

    public UserResponse getById(UUID id) {
        BankUser user = bankUserRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id.toString()));
        return toResponse(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        BankUser user = bankUserRepository.findByUsername(username)
                .orElseThrow(()-> new UsernameNotFoundException("User not found with username: " + username));

        GrantedAuthority authority = new SimpleGrantedAuthority(
                user.getUserRole().getRoleType().name()
        );
        return new User(user.getUsername(), user.getPassword(), Collections.singletonList(authority));
    }

    public void blockUser(UUID id) {
        BankUser user = bankUserRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setBlocked(true);
        bankUserRepository.save(user);
    }

    public void unblockUser(UUID id) {
        BankUser user = bankUserRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setBlocked(false);
        bankUserRepository.save(user);
    }

    public void updateUser(UUID id, UserUpdateRequest request) throws RoleNotFoundException {
        BankUser user = bankUserRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (request.name() != null) {
            user.setName(request.name());
        }
        if (request.email() != null) {
            user.setEmail(request.email());
        }
        if (request.roleId() != null) {
            UserRole role = userRoleRepository.findById(request.roleId())
                    .orElseThrow(() -> new RoleNotFoundException("Role not found with ID: " + request.roleId()));
            user.setUserRole(role);
        }
        if (request.blocked() != null) {
            user.setBlocked(request.blocked());
        }

        bankUserRepository.save(user);
    }

    public void deleteUser(UUID id) {
        BankUser user = bankUserRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id.toString()));
        bankUserRepository.delete(user);
    }

    public UserResponse toResponse(BankUser user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getName(), user.getEmail());
    }
}
