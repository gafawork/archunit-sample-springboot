package com.example.user.application.usecase;

import com.example.user.domain.service.UserDomainService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteUserUseCase {

    private final UserDomainService userDomainService;

    public DeleteUserUseCase(UserDomainService userDomainService) {
        this.userDomainService = userDomainService;
    }

    @Caching(evict = {
        @CacheEvict(value = "users", key = "#id"),
        @CacheEvict(value = "usersList", allEntries = true)
    })
    @Transactional
    public void execute(Long id) {
        userDomainService.findUserById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        
        userDomainService.deleteUser(id);
    }
}
