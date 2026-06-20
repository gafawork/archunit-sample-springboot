# 🚀 Guia Rápido - Testes de API

## 📋 Sumário Rápido dos Endpoints

```
BASE URL: http://localhost:8080/api/v1/users
```

| # | Método | Endpoint | Descrição | Status |
|---|--------|----------|-----------|--------|
| 1 | POST | `/api/v1/users` | Criar usuário | 201 |
| 2 | GET | `/api/v1/users` | Listar usuários | 200 |
| 3 | GET | `/api/v1/users/{id}` | Obter por ID | 200 |
| 4 | PUT | `/api/v1/users/{id}` | Atualizar | 200 |
| 5 | DELETE | `/api/v1/users/{id}` | Deletar | 204 |

---

## 🛠️ Testes Rápidos com cURL

### 1. Criar Usuário
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "firstName": "Teste",
    "lastName": "User",
    "password": "Password123"
  }'
```

### 2. Listar Todos
```bash
curl -X GET http://localhost:8080/api/v1/users
```

### 3. Obter por ID
```bash
curl -X GET http://localhost:8080/api/v1/users/1
```

### 4. Atualizar
```bash
curl -X PUT http://localhost:8080/api/v1/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "NovoNome",
    "lastName": "SobreNome"
  }'
```

### 5. Deletar
```bash
curl -X DELETE http://localhost:8080/api/v1/users/1
```

---

## 📊 Códigos de Resposta HTTP

| Código | Significado | Exemplo |
|--------|------------|---------|
| **201** | Criado com sucesso | POST /users (sucesso) |
| **200** | OK | GET /users (sucesso) |
| **204** | Sem conteúdo | DELETE /users/1 (sucesso) |
| **400** | Requisição inválida | Email inválido |
| **404** | Não encontrado | ID não existe |
| **500** | Erro do servidor | Erro inesperado |

---

## ✅ Validações Importantes

### Email
- Deve ser um email válido: `nome@dominio.com`
- Não pode estar vazio
- Não pode conter caracteres especiais inválidos

### Senha
- Mínimo 8 caracteres
- Pode conter letras, números e símbolos
- Obrigatória na criação, ignorada nas atualizações

### Nome
- Máximo 50 caracteres
- Não pode estar vazio
- Aceita letras, números e alguns caracteres especiais

---

## 🧪 Teste Completo em Minutos

### Passo 1: Iniciar o servidor
```bash
mvn spring-boot:run
```

### Passo 2: Criar um usuário
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "maria@example.com",
    "firstName": "Maria",
    "lastName": "Silva",
    "password": "MariaSilva123"
  }'
```

Copie o `"id"` da resposta.

### Passo 3: Buscar o usuário (substituir 1 pelo ID)
```bash
curl -X GET http://localhost:8080/api/v1/users/1
```

### Passo 4: Atualizar
```bash
curl -X PUT http://localhost:8080/api/v1/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Maria Atualizada"
  }'
```

### Passo 5: Deletar
```bash
curl -X DELETE http://localhost:8080/api/v1/users/1
```

---

## 🔍 Usar Postman (5 Minutos)

1. **Abrir Postman**
2. **Criar Collection:** "User Management"
3. **Criar Requests:**
   - Name: Create User | Method: POST | URL: `http://localhost:8080/api/v1/users`
   - Name: List Users | Method: GET | URL: `http://localhost:8080/api/v1/users`
   - Name: Get User | Method: GET | URL: `http://localhost:8080/api/v1/users/{{id}}`
   - Name: Update User | Method: PUT | URL: `http://localhost:8080/api/v1/users/{{id}}`
   - Name: Delete User | Method: DELETE | URL: `http://localhost:8080/api/v1/users/{{id}}`

4. **Para POST/PUT, adicionar Body (raw JSON):**
```json
{
  "email": "test@example.com",
  "firstName": "Test",
  "lastName": "User",
  "password": "TestPassword123"
}
```

---

## 📝 Usar REST Client IntelliJ (5 Minutos)

1. Criar arquivo `test.http` na raiz do projeto
2. Copiar e colar:

```http
### Variáveis
@base = http://localhost:8080
@userId = 1

### Create
POST {{base}}/api/v1/users
Content-Type: application/json

{
  "email": "test@example.com",
  "firstName": "Test",
  "lastName": "User",
  "password": "Password123"
}

### List
GET {{base}}/api/v1/users

### Get by ID
GET {{base}}/api/v1/users/{{userId}}

### Update
PUT {{base}}/api/v1/users/{{userId}}
Content-Type: application/json

{
  "firstName": "Updated"
}

### Delete
DELETE {{base}}/api/v1/users/{{userId}}
```

3. Clicar em "Run" ao lado de cada requisição

---

## 🧑‍💻 Executar Testes Automatizados

### Todos os testes
```bash
mvn test
```

### Apenas testes de integração da API
```bash
mvn test -Dtest=UserControllerIntegrationTest
```

### Apenas testes de arquitetura
```bash
mvn test -Dtest=*ArchitectureTest
```

### Com relatório de cobertura
```bash
mvn clean test jacoco:report
```

---

## 🐛 Troubleshooting

### Erro: "Connection refused"
- ✓ Verifique se o servidor está rodando: `mvn spring-boot:run`
- ✓ Verifique a porta (padrão 8080)

### Erro: 400 - Bad Request
- ✓ Email inválido? Use formato: `nome@dominio.com`
- ✓ Senha muito curta? Mínimo 8 caracteres
- ✓ JSON malformado? Use validador de JSON

### Erro: 404 - Not Found
- ✓ Usuário realmente existe?
- ✓ URL correta? `/api/v1/users/{id}`

### Erro: 500 - Internal Server Error
- ✓ Veja os logs do servidor
- ✓ Verificar database (H2)

---

## 📚 Arquivo de Requisições

Use o arquivo `requests.http` fornecido para mais exemplos:
```
📄 requests.http
```

---

## 🔨 Script de Testes Bash

Execute o script fornecido (Linux/Mac):
```bash
bash api_tests.sh
```

Ou no PowerShell (Windows):
```powershell
# Converter os comandos cURL para PowerShell
```

---

## 💡 Dicas Rápidas

### Salvar resposta em arquivo
```bash
curl -X GET http://localhost:8080/api/v1/users > resposta.json
```

### Ver apenas headers da resposta
```bash
curl -i -X GET http://localhost:8080/api/v1/users
```

### Ver no Postman
1. Enviar requisição
2. Clicar em "Generate Code"
3. Escolher a linguagem desejada

### Ver no IntelliJ
1. Abrir arquivo `requests.http`
2. Clicar em "Run" (ícone verde)
3. Resultado aparece na aba "REST Client"

---

## 📞 Recursos Adicionais

- 📖 [Documentação Completa](API_SERVICES_TESTING.md)
- 📄 [Exemplos em Java](src/test/java/com/example/user/presentation/controller/UserControllerIntegrationTest.java)
- 🔄 [Requisições HTTP](requests.http)
- 🐚 [Script de Testes](api_tests.sh)

---

## ⏱️ Tempo de Setup

| Ferramenta | Tempo de Setup | Tempo por Teste |
|-----------|----------------|-----------------|
| cURL | 0 min | 30 seg |
| Postman | 5 min | 1 min |
| IntelliJ REST | 2 min | 30 seg |
| JUnit Testes | 10 min | 2-5 seg |
| Script Bash | 2 min | 5 min |

---

## 🎯 Próximos Passos

1. ✓ Iniciar servidor
2. ✓ Executar primeiro CREATE
3. ✓ Tentar READ
4. ✓ Fazer UPDATE
5. ✓ Fazer DELETE
6. ✓ Testar validações (400)
7. ✓ Testar erros (404)
8. ✓ Executar testes JUnit
9. ✓ Verificar cobertura

---

**Última atualização:** 09 de junho de 2026
**Versão:** 1.0 - Guia Rápido

