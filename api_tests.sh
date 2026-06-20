#!/bin/bash

###============================================
### Script de Testes da API com cURL
### Descrição: Exemplos de testes da API User Management
### Uso: bash api_tests.sh
###============================================

# Definir cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configurações
BASE_URL="http://localhost:8080"
API_ENDPOINT="$BASE_URL/api/v1/users"
CONTENT_TYPE="Content-Type: application/json"

# Funções auxiliares
print_header() {
  echo -e "\n${BLUE}======================================${NC}"
  echo -e "${BLUE}$1${NC}"
  echo -e "${BLUE}======================================${NC}\n"
}

print_success() {
  echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
  echo -e "${RED}✗ $1${NC}"
}

print_info() {
  echo -e "${YELLOW}ℹ $1${NC}"
}

# Verificar se a aplicação está rodando
check_server() {
  print_header "Verificando Conexão com Servidor"

  if curl -s -f "$BASE_URL" > /dev/null 2>&1; then
    print_success "Servidor está respondendo em $BASE_URL"
  else
    print_error "Servidor não está disponível em $BASE_URL"
    print_info "Execute a aplicação com: mvn spring-boot:run"
    exit 1
  fi
}

# 1️⃣ Teste: Criar Usuário
test_create_user() {
  print_header "TESTE 1: Criar Novo Usuário"

  RESPONSE=$(curl -s -X POST "$API_ENDPOINT" \
    -H "$CONTENT_TYPE" \
    -d '{
      "email": "teste.curl@example.com",
      "firstName": "Teste",
      "lastName": "cURL",
      "password": "TestPassword123"
    }')

  echo "Response:"
  echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"

  # Extrair ID para usar em testes posteriores
  USER_ID=$(echo "$RESPONSE" | jq -r '.id' 2>/dev/null)

  if [ ! -z "$USER_ID" ] && [ "$USER_ID" != "null" ]; then
    print_success "Usuário criado com ID: $USER_ID"
    echo "$USER_ID"
  else
    print_error "Falha ao criar usuário"
  fi
}

# 2️⃣ Teste: Listar Usuários
test_list_users() {
  print_header "TESTE 2: Listar Todos os Usuários"

  RESPONSE=$(curl -s -X GET "$API_ENDPOINT" \
    -H "Accept: application/json")

  echo "Response:"
  echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"

  COUNT=$(echo "$RESPONSE" | jq 'length' 2>/dev/null)
  if [ ! -z "$COUNT" ]; then
    print_success "Total de usuários: $COUNT"
  fi
}

# 3️⃣ Teste: Obter Usuário por ID
test_get_user() {
  print_header "TESTE 3: Obter Usuário por ID"

  if [ $# -eq 0 ]; then
    print_error "ID do usuário não fornecido"
    return
  fi

  USER_ID=$1
  print_info "Buscando usuário com ID: $USER_ID"

  RESPONSE=$(curl -s -X GET "$API_ENDPOINT/$USER_ID" \
    -H "Accept: application/json")

  echo "Response:"
  echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"

  EMAIL=$(echo "$RESPONSE" | jq -r '.email' 2>/dev/null)
  if [ ! -z "$EMAIL" ] && [ "$EMAIL" != "null" ]; then
    print_success "Usuário encontrado: $EMAIL"
  else
    print_error "Usuário não encontrado ou erro na resposta"
  fi
}

# 4️⃣ Teste: Atualizar Usuário
test_update_user() {
  print_header "TESTE 4: Atualizar Usuário"

  if [ $# -eq 0 ]; then
    print_error "ID do usuário não fornecido"
    return
  fi

  USER_ID=$1
  print_info "Atualizando usuário com ID: $USER_ID"

  RESPONSE=$(curl -s -X PUT "$API_ENDPOINT/$USER_ID" \
    -H "$CONTENT_TYPE" \
    -d '{
      "firstName": "Teste Atualizado",
      "lastName": "cURL Modificado"
    }')

  echo "Response:"
  echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"

  FULL_NAME=$(echo "$RESPONSE" | jq -r '.fullName' 2>/dev/null)
  if [ ! -z "$FULL_NAME" ] && [ "$FULL_NAME" != "null" ]; then
    print_success "Usuário atualizado: $FULL_NAME"
  else
    print_error "Falha ao atualizar usuário"
  fi
}

# 5️⃣ Teste: Deletar Usuário
test_delete_user() {
  print_header "TESTE 5: Deletar Usuário"

  if [ $# -eq 0 ]; then
    print_error "ID do usuário não fornecido"
    return
  fi

  USER_ID=$1
  print_info "Deletando usuário com ID: $USER_ID"

  HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" \
    -X DELETE "$API_ENDPOINT/$USER_ID")

  if [ "$HTTP_CODE" == "204" ]; then
    print_success "Usuário deletado com sucesso (HTTP $HTTP_CODE)"
  else
    print_error "Falha ao deletar usuário (HTTP $HTTP_CODE)"
  fi
}

# 🧪 Teste: Validação de Email Inválido
test_invalid_email() {
  print_header "TESTE 6: Validação - Email Inválido"

  RESPONSE=$(curl -s -X POST "$API_ENDPOINT" \
    -H "$CONTENT_TYPE" \
    -d '{
      "email": "email-invalido@",
      "firstName": "Teste",
      "lastName": "Email",
      "password": "TestPassword123"
    }')

  echo "Response:"
  echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"

  STATUS=$(echo "$RESPONSE" | jq -r '.status' 2>/dev/null)
  if [ "$STATUS" == "400" ]; then
    print_success "Validação de email funcionando corretamente (HTTP 400)"
  else
    print_error "Validação de email não funcionou"
  fi
}

# 🧪 Teste: Senha Curta
test_short_password() {
  print_header "TESTE 7: Validação - Senha Muito Curta"

  RESPONSE=$(curl -s -X POST "$API_ENDPOINT" \
    -H "$CONTENT_TYPE" \
    -d '{
      "email": "senha.curta@example.com",
      "firstName": "Teste",
      "lastName": "Senha",
      "password": "123"
    }')

  echo "Response:"
  echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"

  STATUS=$(echo "$RESPONSE" | jq -r '.status' 2>/dev/null)
  if [ "$STATUS" == "400" ]; then
    print_success "Validação de senha funcionando corretamente (HTTP 400)"
  else
    print_error "Validação de senha não funcionou"
  fi
}

# 🧪 Teste: Usuário Não Encontrado
test_not_found() {
  print_header "TESTE 8: Erro - Usuário Não Encontrado"

  HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" \
    -X GET "$API_ENDPOINT/999999" \
    -H "Accept: application/json")

  if [ "$HTTP_CODE" == "404" ]; then
    print_success "Usuário não encontrado retornado corretamente (HTTP 404)"
  else
    print_error "Esperado HTTP 404, recebido: $HTTP_CODE"
  fi
}

# 🔬 Teste: Fluxo Completo CRUD
test_full_crud_flow() {
  print_header "TESTE 9: Fluxo Completo CRUD"

  print_info "Passo 1: Criando usuário..."
  USER_ID=$(test_create_user | tail -n 1)

  if [ -z "$USER_ID" ] || [ "$USER_ID" == "null" ]; then
    print_error "Falha ao criar usuário no fluxo CRUD"
    return
  fi

  print_info "Passo 2: Buscando usuário criado..."
  test_get_user "$USER_ID"

  print_info "Passo 3: Atualizando usuário..."
  test_update_user "$USER_ID"

  print_info "Passo 4: Obter usuário após atualização..."
  test_get_user "$USER_ID"

  print_info "Passo 5: Deletando usuário..."
  test_delete_user "$USER_ID"

  print_success "Fluxo CRUD concluído com sucesso!"
}

# 📊 Teste: Status HTTP
test_http_status_codes() {
  print_header "TESTE 10: Verificar Status HTTP"

  print_info "POST (deve retornar 201 Created)"
  STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
    -X POST "$API_ENDPOINT" \
    -H "$CONTENT_TYPE" \
    -d '{
      "email": "status.test@example.com",
      "firstName": "Status",
      "lastName": "Test",
      "password": "StatusTest123"
    }')
  [ "$STATUS" == "201" ] && print_success "POST: $STATUS" || print_error "POST: $STATUS (esperado 201)"

  print_info "GET (deve retornar 200 OK)"
  STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
    -X GET "$API_ENDPOINT" \
    -H "Accept: application/json")
  [ "$STATUS" == "200" ] && print_success "GET: $STATUS" || print_error "GET: $STATUS (esperado 200)"

  print_info "GET inválido (deve retornar 404 Not Found)"
  STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
    -X GET "$API_ENDPOINT/999999" \
    -H "Accept: application/json")
  [ "$STATUS" == "404" ] && print_success "GET 404: $STATUS" || print_error "GET 404: $STATUS (esperado 404)"
}

# 🚀 Menu Principal
main() {
  echo -e "${BLUE}"
  echo "╔════════════════════════════════════════╗"
  echo "║   User Management API - Testes cURL    ║"
  echo "╚════════════════════════════════════════╝"
  echo -e "${NC}"

  check_server

  # Executar todos os testes
  test_http_status_codes
  test_list_users
  test_invalid_email
  test_short_password
  test_not_found
  test_full_crud_flow

  print_header "Testes Concluídos!"
  print_success "Todos os testes foram executados"
}

# Executar
main "$@"

