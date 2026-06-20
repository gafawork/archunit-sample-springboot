package com.example.user.application.usecase;

import com.example.user.application.dto.UserResponse;
import com.example.user.domain.entity.User;
import com.example.user.domain.service.UserDomainService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListUsersUseCase {

    private final UserDomainService userDomainService;

    public ListUsersUseCase(UserDomainService userDomainService) {
        this.userDomainService = userDomainService;
    }

    @Cacheable(value = "usersList")
    public List<UserResponse> execute() {
        return userDomainService.getAllUsers()
                .stream()
                .map(this::toResponse)
                .toList();
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
