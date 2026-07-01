package com.example.user.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

@DisplayName("User Entity Dynamic Tests")
class UserDynamicTest {

    @TestFactory
    @DisplayName("Criar usuarios com diferentes dados válidos")
    Collection<DynamicTest> createValidUsers() {
        return Arrays.asList(
            dynamicTest("Criar usuário com nome simples", () -> {
                User user = new User("john@example.com", "John", "Doe", "password123");
                assertEquals("john@example.com", user.getEmail());
                assertEquals("John Doe", user.getFullName());
                assertTrue(user.getActive());
            }),
            dynamicTest("Criar usuário com caracteres especiais no nome", () -> {
                User user = new User("maria@example.com", "María", "López", "senha456");
                assertEquals("maria@example.com", user.getEmail());
                assertEquals("María López", user.getFullName());
                assertTrue(user.getActive());
            }),
            dynamicTest("Criar usuário com nomes longos", () -> {
                User user = new User("test@example.com", "Christopher", "Alexandersson", "pass789");
                assertEquals("test@example.com", user.getEmail());
                assertTrue(user.getFullName().length() > 20);
            }),
            dynamicTest("Criar usuário com email corporativo", () -> {
                User user = new User("employee@company.com", "Jane", "Smith", "corporate");
                assertEquals("employee@company.com", user.getEmail());
                assertTrue(user.getEmail().contains("@company.com"));
            })
        );
    }

    @TestFactory
    @DisplayName("Testar ativação/desativação de usuários")
    Stream<DynamicTest> activateDeactivateUser() {
        User user = new User("admin@example.com", "Admin", "User", "adminpass");

        return Stream.of(
            dynamicTest("Usuário novo deve estar ativo por padrão", () -> {
                assertTrue(user.getActive(), "Novo usuário deve estar ativo");
            }),
            dynamicTest("Desativar usuário ativo", () -> {
                user.deactivate();
                assertFalse(user.getActive(), "Usuário deve estar inativo após deactivate()");
            }),
            dynamicTest("Reativar usuário desativado", () -> {
                user.activate();
                assertTrue(user.getActive(), "Usuário deve estar ativo após activate()");
            }),
            dynamicTest("Desativar novamente", () -> {
                user.deactivate();
                assertFalse(user.getActive(), "Usuário deve estar inativo");
            })
        );
    }

    @TestFactory
    @DisplayName("Validar nomes completos com diferentes combinações")
    Collection<DynamicTest> validateFullNames() {
        return Arrays.asList(
            dynamicTest("Nome com inicial maiúscula", () -> {
                User user = new User("test@test.com", "João", "Silva", "pass");
                assertEquals("João Silva", user.getFullName());
            }),
            dynamicTest("Nome com múltiplas palavras", () -> {
                User user = new User("test@test.com", "Carlos", "Alberto Santos", "pass");
                assertTrue(user.getFullName().contains("Carlos"));
                assertTrue(user.getFullName().contains("Alberto Santos"));
            }),
            dynamicTest("Validar formato esperado do nome completo", () -> {
                User user = new User("test@test.com", "Ana", "Costa", "pass");
                assertTrue(user.getFullName().matches("[A-Za-záéíóúàâãôõçñ]+ [A-Za-záéíóúàâãôõçñ]+"));
            })
        );
    }

    @TestFactory
    @DisplayName("Testar construtor com Builder")
    Collection<DynamicTest> testUserBuilder() {
        return Arrays.asList(
            dynamicTest("Builder com todos os campos", () -> {
                User user = User.builder()
                    .id(1L)
                    .email("builder@example.com")
                    .firstName("Builder")
                    .lastName("Test")
                    .password("builderpass")
                    .active(true)
                    .build();

                assertEquals(1L, user.getId());
                assertEquals("builder@example.com", user.getEmail());
                assertEquals("Builder Test", user.getFullName());
                assertTrue(user.getActive());
            }),
            dynamicTest("Builder configurando usuário inativo", () -> {
                User user = User.builder()
                    .email("inactive@example.com")
                    .firstName("Inactive")
                    .lastName("User")
                    .password("pass")
                    .active(false)
                    .build();

                assertFalse(user.getActive());
            })
        );
    }

    @TestFactory
    @DisplayName("Testes de múltiplas operações em sequência")
    Stream<DynamicTest> multipleOperations() {
        return Stream.of(
            dynamicTest("Criar, desativar e reativar usuário", () -> {
                User user = new User("sequence@test.com", "Sequence", "Test", "pass");
                assertTrue(user.getActive());

                user.deactivate();
                assertFalse(user.getActive());

                user.activate();
                assertTrue(user.getActive());
            }),
            dynamicTest("Validar email após criação", () -> {
                User user = new User("validation@test.com", "Val", "Test", "pass");
                assertNotNull(user.getEmail());
                assertTrue(user.getEmail().contains("@"));
                assertEquals("validation@test.com", user.getEmail());
            }),
            dynamicTest("Validar nome completo não é nulo", () -> {
                User user = new User("notnull@test.com", "Not", "Null", "pass");
                assertNotNull(user.getFullName());
                assertFalse(user.getFullName().isEmpty());
                assertFalse(user.getFullName().isBlank());
            })
        );
    }
}
