package com.example.user.application.usecase;

import com.example.user.application.dto.UpdateUserRequest;
import com.example.user.application.dto.UserResponse;
import com.example.user.domain.entity.User;
import com.example.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifica o comportamento do cache (hit e eviction) sobre os use cases.
 *
 * <p>Usa {@code spring.cache.type=simple} (cache em memória), dispensando o Redis
 * durante os testes. O {@link UserRepository} é mockado para contar quantas vezes
 * o backend é realmente consultado.</p>
 */
@SpringBootTest(properties = "spring.cache.type=simple")
class UserCacheIntegrationTest {

    @Autowired
    private GetUserByIdUseCase getUserByIdUseCase;

    @Autowired
    private UpdateUserUseCase updateUserUseCase;

    @Autowired
    private DeleteUserUseCase deleteUserUseCase;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private UserRepository userRepository;

    @BeforeEach
    void clearCaches() {
        cacheManager.getCacheNames()
            .forEach(name -> cacheManager.getCache(name).clear());
    }

    private User sampleUser() {
        return User.builder()
            .id(1L)
            .email("john@example.com")
            .firstName("John")
            .lastName("Doe")
            .password("secret")
            .active(true)
            .build();
    }

    @Test
    void deve_servir_a_segunda_leitura_a_partir_do_cache() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser()));

        UserResponse first = getUserByIdUseCase.execute(1L);
        UserResponse second = getUserByIdUseCase.execute(1L);

        assertThat(first.getId()).isEqualTo(1L);
        assertThat(second.getEmail()).isEqualTo("john@example.com");
        // A segunda chamada foi servida pelo cache: o repositório foi consultado uma única vez.
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void update_deve_invalidar_o_cache_do_usuario() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser()));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        getUserByIdUseCase.execute(1L);            // popula o cache (users::1)
        updateUserUseCase.execute(1L,              // invalida users::1
            UpdateUserRequest.builder().firstName("Jane").lastName("Doe").build());
        getUserByIdUseCase.execute(1L);            // recarrega do repositório

        // findById: 1x leitura inicial + 1x dentro do update + 1x após a eviction = 3
        verify(userRepository, times(3)).findById(1L);
    }

    @Test
    void delete_deve_invalidar_o_cache_do_usuario() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser()));

        getUserByIdUseCase.execute(1L);            // popula o cache (users::1)
        deleteUserUseCase.execute(1L);             // invalida users::1
        getUserByIdUseCase.execute(1L);            // recarrega do repositório

        // findById: 1x leitura inicial + 1x dentro do delete + 1x após a eviction = 3
        verify(userRepository, times(3)).findById(1L);
    }
}
