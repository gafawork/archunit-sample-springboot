# 📚 Documentação de Serviços de API e Testes

## Índice
1. [Visão Geral](#visão-geral)
2. [Serviços de API](#serviços-de-api)
3. [Como Testar a API](#como-testar-a-api)
4. [Exemplos de Testes Prácticos](#exemplos-de-testes-práticos)
5. [Ferramentas Recomendadas](#ferramentas-recomendadas)

---

## 🎯 Visão Geral

Este documento fornece um guia completo sobre como utilizar os serviços de API REST da aplicação **User Management API** e como realizar testes eficazes.

### Tecnologias Utilizadas
- **Spring Boot** 4.0.6 (com Spring Web, Spring Data JPA, Spring Cache)
- **Java** 21
- **H2 Database** (banco de dados em memória)
- **Redis** (para cache em produção)
- **JUnit 5** e **Mockito** (para testes)
- **ArchUnit** 1.4.2 (para testes de arquitetura)

---

## 🚀 Serviços de API

### Base URL
```
http://localhost:8080/api/v1/users
```

### Endpoints Disponíveis

A aplicação fornece 5 endpoints principais para gerenciar usuários:

| Método | Endpoint | Descrição | Status HTTP |
|--------|----------|-----------|-------------|
| **POST** | `/api/v1/users` | Criar novo usuário | 201 Created |
| **GET** | `/api/v1/users` | Listar todos os usuários | 200 OK |
| **GET** | `/api/v1/users/{id}` | Obter usuário por ID | 200 OK |
| **PUT** | `/api/v1/users/{id}` | Atualizar usuário | 200 OK |
| **DELETE** | `/api/v1/users/{id}` | Deletar usuário | 204 No Content |

---

### 1️⃣ Criar Usuário (POST)

**Endpoint:** `POST /api/v1/users`

**Descrição:** Cria um novo usuário no sistema.

**Request Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "password": "securePassword123"
}
```

**Validações:**
- `email`: Deve ser um email válido e único
- `firstName`: Obrigatório, máximo 50 caracteres
- `lastName`: Obrigatório, máximo 50 caracteres
- `password`: Obrigatório, mínimo 8 caracteres

**Response (201 Created):**
```json
{
  "id": 1,
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "fullName": "John Doe",
  "active": true
}
```

**Response (400 Bad Request) - Validação Falhou:**
```json
{
  "message": "Validation failed",
  "status": 400,
  "timestamp": "2026-06-09T10:30:00",
  "errors": {
    "email": "Email should be valid",
    "password": "Password must have at least 8 characters"
  }
}
```

---

### 2️⃣ Listar Todos os Usuários (GET)

**Endpoint:** `GET /api/v1/users`

**Descrição:** Recupera a lista de todos os usuários cadastrados no sistema.

**Request Headers:**
```
Accept: application/json
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "fullName": "John Doe",
    "active": true
  },
  {
    "id": 2,
    "email": "jane.smith@example.com",
    "firstName": "Jane",
    "lastName": "Smith",
    "fullName": "Jane Smith",
    "active": true
  }
]
```

**Response (200 OK) - Lista Vazia:**
```json
[]
```

---

### 3️⃣ Obter Usuário por ID (GET)

**Endpoint:** `GET /api/v1/users/{id}`

**Descrição:** Recupera os detalhes de um usuário específico pelo seu ID.

**Parâmetros:**
- `id` (Path Parameter): ID do usuário (número inteiro)

**Response (200 OK):**
```json
{
  "id": 1,
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "fullName": "John Doe",
  "active": true
}
```

**Response (404 Not Found):**
```json
{
  "message": "User not found",
  "status": 404,
  "timestamp": "2026-06-09T10:30:00"
}
```

---

### 4️⃣ Atualizar Usuário (PUT)

**Endpoint:** `PUT /api/v1/users/{id}`

**Descrição:** Atualiza os dados de um usuário existente.

**Parâmetros:**
- `id` (Path Parameter): ID do usuário a ser atualizado

**Request Body (todos os campos são opcionais):**
```json
{
  "firstName": "Jonathan",
  "lastName": "Doe"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "email": "john.doe@example.com",
  "firstName": "Jonathan",
  "lastName": "Doe",
  "fullName": "Jonathan Doe",
  "active": true
}
```

**Response (404 Not Found):**
```json
{
  "message": "User not found",
  "status": 404,
  "timestamp": "2026-06-09T10:30:00"
}
```

---

### 5️⃣ Deletar Usuário (DELETE)

**Endpoint:** `DELETE /api/v1/users/{id}`

**Descrição:** Remove um usuário do sistema.

**Parâmetros:**
- `id` (Path Parameter): ID do usuário a ser deletado

**Response (204 No Content):**
Sem corpo na resposta.

**Response (404 Not Found):**
```json
{
  "message": "User not found",
  "status": 404,
  "timestamp": "2026-06-09T10:30:00"
}
```

---

## 🧪 Como Testar a API

### Opção 1: Usando cURL (Linha de Comando)

#### 1.1 Criar Usuário
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "teste@example.com",
    "firstName": "Teste",
    "lastName": "User",
    "password": "Password123"
  }'
```

#### 1.2 Listar Usuários
```bash
curl -X GET http://localhost:8080/api/v1/users \
  -H "Accept: application/json"
```

#### 1.3 Obter Usuário por ID
```bash
curl -X GET http://localhost:8080/api/v1/users/1 \
  -H "Accept: application/json"
```

#### 1.4 Atualizar Usuário
```bash
curl -X PUT http://localhost:8080/api/v1/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "NovoNome",
    "lastName": "SobreNome"
  }'
```

#### 1.5 Deletar Usuário
```bash
curl -X DELETE http://localhost:8080/api/v1/users/1
```

---

### Opção 2: Usando Postman

#### Setup Inicial
1. **Abrir Postman**
2. **Criar uma nova Collection:** "User Management API"
3. **Criar um Environment:** "Development"
4. **Definir variável:** `base_url = http://localhost:8080`

#### Exemplos de Requisições

**CREATE User**
- Method: `POST`
- URL: `{{base_url}}/api/v1/users`
- Headers: `Content-Type: application/json`
- Body (raw JSON):
```json
{
  "email": "mariana.silva@example.com",
  "firstName": "Mariana",
  "lastName": "Silva",
  "password": "SecurePass123"
}
```

**GET List Users**
- Method: `GET`
- URL: `{{base_url}}/api/v1/users`

**GET User by ID**
- Method: `GET`
- URL: `{{base_url}}/api/v1/users/{{user_id}}`

**UPDATE User**
- Method: `PUT`
- URL: `{{base_url}}/api/v1/users/{{user_id}}`
- Headers: `Content-Type: application/json`
- Body (raw JSON):
```json
{
  "firstName": "MarianaUpdated",
  "lastName": "SilvaUpdated"
}
```

**DELETE User**
- Method: `DELETE`
- URL: `{{base_url}}/api/v1/users/{{user_id}}`

---

### Opção 3: Usando IntelliJ IDEA REST Client

Crie um arquivo `requests.http` na raiz do projeto:

```http
### Variáveis
@baseUrl = http://localhost:8080
@userId = 1

### 1. Criar novo usuário
POST {{baseUrl}}/api/v1/users
Content-Type: application/json

{
  "email": "usuario.novo@example.com",
  "firstName": "Novo",
  "lastName": "Usuário",
  "password": "Password123"
}

### 2. Listar todos os usuários
GET {{baseUrl}}/api/v1/users
Accept: application/json

### 3. Obter usuário específico
GET {{baseUrl}}/api/v1/users/{{userId}}
Accept: application/json

### 4. Atualizar usuário
PUT {{baseUrl}}/api/v1/users/{{userId}}
Content-Type: application/json

{
  "firstName": "Atualizado",
  "lastName": "Nome"
}

### 5. Deletar usuário
DELETE {{baseUrl}}/api/v1/users/{{userId}}
```

---

## 🔬 Exemplos de Testes Práticos

### Tipo 1: Testes de Integração com @SpringBootTest

Estes testes verificam o comportamento completo da aplicação, incluindo a camada HTTP, controladores e use cases.

#### Exemplo: Teste de Cache de Usuários

```java
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
import static org.mockito.Mockito.*;

/**
 * Verifica o comportamento do cache (hit e eviction) sobre os use cases.
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
        
        // A segunda chamada foi servida pelo cache: 
        // o repositório foi consultado apenas uma vez.
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void update_deve_invalidar_o_cache_do_usuario() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser()));
        when(userRepository.save(any(User.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        getUserByIdUseCase.execute(1L);  // popula o cache
        updateUserUseCase.execute(1L,    // invalida cache
            UpdateUserRequest.builder()
                .firstName("Jane")
                .lastName("Doe")
                .build()
        );
        getUserByIdUseCase.execute(1L);  // recarrega do repositório

        // findById chamado: 1x inicial + 1x no update + 1x após eviction = 3
        verify(userRepository, times(3)).findById(1L);
    }

    @Test
    void delete_deve_invalidar_o_cache_do_usuario() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser()));

        getUserByIdUseCase.execute(1L);     // popula o cache
        deleteUserUseCase.execute(1L);      // invalida cache
        getUserByIdUseCase.execute(1L);     // recarrega do repositório

        // findById chamado 3 vezes
        verify(userRepository, times(3)).findById(1L);
    }
}
```

---

### Tipo 2: Testes de Arquitetura

Estes testes verificam se a aplicação segue padrões de arquitetura definidos.

#### Exemplo: Teste de Controller REST

```java
package com.example.user.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

/**
 * Regras específicas de REST para os controllers.
 */
@AnalyzeClasses(packages = "com.example.user", 
                importOptions = ImportOption.DoNotIncludeTests.class)
class RestControllerArchitectureTest {

    @ArchTest
    static final ArchRule controllers_devem_ter_request_mapping =
        classes().that().areAnnotatedWith(RestController.class)
            .should().beAnnotatedWith(RequestMapping.class)
            .as("REST controllers devem ter @RequestMapping");

    @ArchTest
    static final ArchRule metodos_nao_devem_ser_estaticos =
        noMethods().that().areDeclaredInClassesThat()
            .areAnnotatedWith(RestController.class)
            .should().beStatic()
            .as("REST controllers não devem ter métodos estáticos");

    @ArchTest
    static final ArchRule metodos_publicos_retornam_response_entity =
        methods().that().areDeclaredInClassesThat()
            .areAnnotatedWith(RestController.class)
            .and().arePublic()
            .should().haveRawReturnType("org.springframework.http.ResponseEntity")
            .as("Métodos públicos (endpoints) devem retornar ResponseEntity");
}
```

---

### Tipo 3: Testes de Camada (Layer Tests)

Verificam se as dependências entre camadas estão corretas.

#### Exemplo: Teste de Camada de Apresentação

```java
package com.example.user.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Service;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Testes de arquitetura da camada de apresentação.
 */
@AnalyzeClasses(packages = "com.example.user", 
                importOptions = ImportOption.DoNotIncludeTests.class)
class PresentationLayerArchitectureTest {

    @ArchTest
    static final ArchRule camada_apresentacao_nao_deve_acessar_domain =
        classes().that().resideInAPackage("..presentation..")
            .should().notDependOnClassesThat()
            .resideInAPackage("..domain..")
            .as("Camada de apresentação não deveria acessar entidades de domínio");

    @ArchTest
    static final ArchRule camada_apresentacao_deve_usar_dtos =
        classes().that().resideInAPackage("..presentation..")
            .should().dependOnClassesThat()
            .resideInAPackage("..application.dto..")
            .as("Controllers devem usar DTOs do application layer");
}
```

---

## 🧑‍💻 Como Executar os Testes

### Executar todos os testes:
```bash
mvn test
```

### Executar um teste específico:
```bash
mvn test -Dtest=UserCacheIntegrationTest
```

### Executar testes de uma classe específica:
```bash
mvn test -Dtest=RestControllerArchitectureTest
```

### Executar com cobertura de código:
```bash
mvn test jacoco:report
```

### Executar apenas testes de integração:
```bash
mvn test -Dgroups=integration
```

---

## 🛠️ Ferramentas Recomendadas

### 1. **Postman**
- **Uso:** Testar manualmente endpoints da API
- **Download:** https://www.postman.com/downloads/
- **Vantagens:**
  - Interface gráfica intuitiva
  - Salva histórico de requisições
  - Suporta environments e variáveis
  - Gera testes automaticamente

### 2. **cURL**
- **Uso:** Testar API via linha de comando
- **Instalado por padrão** em sistemas Unix/Linux
- **Vantagens:**
  - Leve e rápido
  - Ideal para automação
  - Não requer instalação

### 3. **IntelliJ IDEA REST Client**
- **Uso:** Testar API dentro da IDE
- **Arquivo:** `requests.http`
- **Vantagens:**
  - Integrado na IDE
  - Sintaxe simples
  - Fácil de compartilhar com o time

### 4. **JUnit 5 + Mockito**
- **Uso:** Testes unitários e de integração
- **Frameworks auxiliares:**
  - AssertJ (assertions fluentes)
  - Spring Boot Test (contexto Spring)
  - ArchUnit (testes de arquitetura)

### 5. **Apache JMeter**
- **Uso:** Testes de carga e performance
- **Download:** https://jmeter.apache.org/
- **Vantagens:**
  - Simula múltiplos usuários simultâneos
  - Gera relatórios de performance
  - Pode simular comportamentos reais

### 6. **Swagger UI**
- **Uso:** Documentação interativa da API
- **URL:** `http://localhost:8080/swagger-ui.html` (quando configurado)
- **Vantagens:**
  - Documenta todos os endpoints automaticamente
  - Permite testar endpoints diretamente
  - Gera especificações OpenAPI

---

## 📊 Fluxo de Teste Recomendado

```
1. Desenvolvimento
   ↓
2. Testes Unitários (Mocks)
   ↓
3. Testes de Integração (SpringBootTest)
   ↓
4. Testes de Arquitetura (ArchUnit)
   ↓
5. Testes Manuais (Postman/cURL)
   ↓
6. Testes de Carga (JMeter)
   ↓
7. Deploy
```

---

## 📋 Checklist de Testes

Antes de fazer deploy, verifique:

- [ ] Todos os testes unitários passam
- [ ] Todos os testes de integração passam
- [ ] Todos os testes de arquitetura passam
- [ ] Cobertura de código acima de 80%
- [ ] Endpoints testados manualmente via Postman
- [ ] Tratamento de erros validado (400, 404, 500)
- [ ] Validações de entrada testadas
- [ ] Performance aprovada em testes de carga
- [ ] Cache funcionando corretamente
- [ ] Documentação atualizada

---

## 🔒 Dicas de Testes de Segurança

### 1. Validar Entrada
```bash
# Teste com caracteres especiais
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "<script>alert(1)</script>@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "password": "Password123"
  }'
```

### 2. Validar SQL Injection
```bash
# Teste com SQL injection attempt
curl -X GET "http://localhost:8080/api/v1/users/1; DROP TABLE users--"
```

### 3. Validar Limites de Tamanho
```bash
# Request muito grande
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "firstName": "A very long string that exceeds maximum length...",
    "lastName": "Doe",
    "password": "Password123"
  }'
```

---

## 📞 Suporte e Referências

- **Spring Boot Documentation:** https://spring.io/projects/spring-boot
- **REST API Best Practices:** https://restfulapi.net/
- **ArchUnit Documentation:** https://www.archunit.org/
- **JUnit 5 Guide:** https://junit.org/junit5/docs/current/user-guide/
- **Mockito Documentation:** https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html

---

**Última atualização:** 09 de junho de 2026
**Versão:** 1.0

