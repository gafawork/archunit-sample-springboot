package com.example.user.application.usecase;

import com.example.user.application.dto.UserResponse;
import com.example.user.domain.entity.User;
import com.example.user.domain.service.UserDomainService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class GetUserByIdUse {

    private final UserDomainService userDomainService;

    public GetUserByIdUse(UserDomainService userDomainService) {
        this.userDomainService = userDomainService;
    }

    @Cacheable(value = "users", key = "#id")
    public UserResponse execute(Long id) {
        User user = userDomainService.findUserById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        
        return toResponse(user);
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
