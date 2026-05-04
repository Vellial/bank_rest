package com.example.bankcards.util;

import com.example.bankcards.entity.BankUser;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.BankUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityUtils {
    private final BankUserRepository bankUserRepository;

    public BankUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();  // username = email


        return bankUserRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }
}
