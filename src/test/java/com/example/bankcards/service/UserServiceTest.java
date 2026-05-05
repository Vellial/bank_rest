package com.example.bankcards.service;

import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.dto.user.UserUpdateRequest;
import com.example.bankcards.entity.BankUser;
import com.example.bankcards.entity.RoleType;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.BankUserRepository;
import com.example.bankcards.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.management.relation.RoleNotFoundException;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private BankUserRepository bankUserRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @InjectMocks
    private UserService userService;

    private int roleId;
    private UUID testUserId;
    private BankUser testUser;
    private UserRole testRole;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        roleId = 10;
        testRole = new UserRole();
        testRole.setId(roleId);
        testRole.setRoleType(RoleType.USER);
        testUser = new BankUser();
        testUser.setId(testUserId);
        testUser.setUsername("testuser");
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setUserRole(testRole);
        testUser.setBlocked(false);
        pageable = mock(Pageable.class);
    }

    @Test
    void getAll_shouldReturnPageOfUserResponses() {
        Page<BankUser> userPage = new PageImpl<>(Collections.singletonList(testUser));
        when(bankUserRepository.findAll(pageable)).thenReturn(userPage);

        Page<UserResponse> result = userService.getAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testUserId, result.getContent().get(0).id());
        assertEquals("testuser", result.getContent().get(0).username());
        assertEquals("Test User", result.getContent().get(0).name());
        assertEquals("test@example.com", result.getContent().get(0).email());
        verify(bankUserRepository).findAll(pageable);
    }

    @Test
    void getById_shouldReturnUserResponse_whenUserExists() {
        when(bankUserRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        UserResponse result = userService.getById(testUserId);

        assertNotNull(result);
        assertEquals(testUserId, result.id());
        assertEquals("testuser", result.username());
        assertEquals("Test User", result.name());
        assertEquals("test@example.com", result.email());
    }

    @Test
    void getById_shouldThrowUserNotFoundException_whenUserNotFound() {
        when(bankUserRepository.findById(testUserId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getById(testUserId));
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {
        when(bankUserRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        UserDetails result = userService.loadUserByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals(testUser.getPassword(), result.getPassword());
        assertEquals("USER", result.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void loadUserByUsername_shouldThrowUsernameNotFoundException_whenUserNotFound() {
        when(bankUserRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("nonexistent"));
    }

    @Test
    void blockUser_shouldSetBlockedToTrue_andSave() {
        when(bankUserRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        userService.blockUser(testUserId);

        assertTrue(testUser.getBlocked());
        verify(bankUserRepository).save(testUser);
    }

    @Test
    void blockUser_shouldThrowUserNotFoundException_whenUserNotFound() {
        when(bankUserRepository.findById(testUserId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.blockUser(testUserId));
    }

    @Test
    void unblockUser_shouldSetBlockedToFalse_andSave() {
        testUser.setBlocked(true);
        when(bankUserRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        userService.unblockUser(testUserId);

        assertFalse(testUser.getBlocked());
        verify(bankUserRepository).save(testUser);
    }

    @Test
    void unblockUser_shouldThrowUserNotFoundException_whenUserNotFound() {
        when(bankUserRepository.findById(testUserId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.unblockUser(testUserId));
    }

    @Test
    void updateUser_shouldUpdateAllFields_whenProvided() throws RoleNotFoundException {
        int newRoleId = 20;
        UserRole newRole = new UserRole();
        newRole.setId(newRoleId);
        newRole.setRoleType(RoleType.ADMIN);

        UserUpdateRequest request = new UserUpdateRequest(
            "updated@example.com",
            "Updated Name",
            newRoleId,
            true
        );

        when(bankUserRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRoleRepository.findById(newRoleId)).thenReturn(Optional.of(newRole));

        userService.updateUser(testUserId, request);

        assertEquals("Updated Name", testUser.getName());
        assertEquals("updated@example.com", testUser.getEmail());
        assertEquals(newRole, testUser.getUserRole());
        assertTrue(testUser.getBlocked());
        verify(bankUserRepository).save(testUser);
    }

    @Test
    void updateUser_shouldNotUpdateNullFields() throws RoleNotFoundException {
        UserUpdateRequest request = new UserUpdateRequest(null, null, null, null);

        when(bankUserRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        userService.updateUser(testUserId, request);

        assertEquals("Test User", testUser.getName());
        assertEquals("test@example.com", testUser.getEmail());
        assertEquals(testRole, testUser.getUserRole());
        assertFalse(testUser.getBlocked());
        verify(bankUserRepository).save(testUser);
    }

    @Test
    void updateUser_shouldThrowRoleNotFoundException_whenRoleNotFound() {
        int invalidRoleId = 999;
        UserUpdateRequest request = new UserUpdateRequest(null, null, invalidRoleId, null);

        when(bankUserRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRoleRepository.findById(invalidRoleId)).thenReturn(Optional.empty());

        assertThrows(RoleNotFoundException.class, () -> userService.updateUser(testUserId, request));
    }

    @Test
    void deleteUser_shouldDeleteUser() {
        when(bankUserRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        userService.deleteUser(testUserId);

        verify(bankUserRepository).delete(testUser);
    }

    @Test
    void deleteUser_shouldThrowUserNotFoundException_whenUserNotFound() {
        when(bankUserRepository.findById(testUserId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(testUserId));
    }

    @Test
    void toResponse_shouldConvertBankUserToUserResponse() {
        UserResponse response = userService.toResponse(testUser);

        assertEquals(testUserId, response.id());
        assertEquals("testuser", response.username());
        assertEquals("Test User", response.name());
        assertEquals("test@example.com", response.email());
    }
}
