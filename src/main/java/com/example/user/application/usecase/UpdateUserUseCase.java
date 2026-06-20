package com.example.user.application.usecase;

import com.example.user.application.dto.UpdateUserRequest;
import com.example.user.application.dto.UserResponse;
import com.example.user.domain.entity.User;
import com.example.user.domain.service.UserDomainService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateUserUseCase {

    private final UserDomainService userDomainService;

    public UpdateUserUseCase(UserDomainService userDomainService) {
        this.userDomainService = userDomainService;
    }

    @Caching(evict = {
        @CacheEvict(value = "users", key = "#id"),
        @CacheEvict(value = "usersList", allEntries = true)
    })
    @Transactional
    public UserResponse execute(Long id, UpdateUserRequest request) {
        User user = userDomainService.findUserById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        
        User updatedUser = userDomainService.saveUser(user);
        return toResponse(updatedUser);
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .fullName(user.getFullName())
            .active(user.getActive())
            .build();
    }
}
