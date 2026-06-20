package com.example.user.presentation.exception;

import com.example.user.application.dto.CreateUserRequest;
import com.example.user.application.dto.UserResponse;
import com.example.user.domain.entity.User;
import com.example.user.domain.service.UserDomainService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateUserUseCase {

    private final UserDomainService userDomainService;

    public CreateUserUseCase(UserDomainService userDomainService) {
        this.userDomainService = userDomainService;
    }

    @CacheEvict(value = "usersList", allEntries = true)
    @Transactional
    public UserResponse execute(CreateUserRequest request) {
        User user = userDomainService.createUser(
            request.getEmail(),
            request.getFirstName(),
            request.getLastName(),
            request.getPassword()
        );
        
        User savedUser = userDomainService.saveUser(user);
        return toResponse(savedUser);
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
