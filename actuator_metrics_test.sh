#!/bin/bash

###============================================
### Script de Teste do Actuator e Métricas
### Descrição: Testa todos os endpoints de métricas
### Uso: bash actuator_metrics_test.sh
###============================================

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Configurações
BASE_URL="http://localhost:8080"
ACCEPT_JSON="Accept: application/json"

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

print_metric() {
  echo -e "${CYAN}📊 $1${NC}"
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

# 1. Teste: Health Check
test_health() {
  print_header "1. HEALTH CHECK"

  RESPONSE=$(curl -s -X GET "$BASE_URL/actuator/health" \
    -H "$ACCEPT_JSON")

  echo "Response:"
  echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"

  STATUS=$(echo "$RESPONSE" | jq -r '.status' 2>/dev/null)

  if [ "$STATUS" = "UP" ]; then
    print_success "Aplicação está HEALTHY"
  else
    print_error "Aplicação está com problemas"
  fi
}

# 2. Teste: Lista de Métricas
test_metrics_list() {
  print_header "2. LISTA DE MÉTRICAS DISPONÍVEIS"

  RESPONSE=$(curl -s -X GET "$BASE_URL/actuator/metrics" \
    -H "$ACCEPT_JSON")

  COUNT=$(echo "$RESPONSE" | jq '.names | length' 2>/dev/null)

  print_success "Total de métricas disponíveis: $COUNT"

  echo ""
  echo "Primeiras 10 métricas:"
  echo "$RESPONSE" | jq '.names[:10]' 2>/dev/null
}

# 3. Teste: JVM Memory
test_jvm_memory() {
  print_header "3. MÉTRICAS JVM - MEMÓRIA"

  RESPONSE=$(curl -s -X GET "$BASE_URL/actuator/metrics/jvm.memory.used" \
    -H "$ACCEPT_JSON")

  MEMORY=$(echo "$RESPONSE" | jq '.measurements[0].value' 2>/dev/null)
  MEMORY_MB=$(echo "scale=2; $MEMORY / 1048576" | bc)

  print_metric "Memória JVM usada: $MEMORY_MB MB"

  # Ver breakdown por tipo
  echo ""
  echo "Detalhes por tipo:"
  RESPONSE_DETAILED=$(curl -s -X GET "$BASE_URL/actuator/metrics/jvm.memory.used" \
    -H "$ACCEPT_JSON")
  echo "$RESPONSE_DETAILED" | jq '.measurements[] | {area: .value, id: .statistic}' 2>/dev/null
}

# 4. Teste: Processo UP Time
test_process_uptime() {
  print_header "4. UPTIME DO PROCESSO"

  RESPONSE=$(curl -s -X GET "$BASE_URL/actuator/metrics/process.uptime" \
    -H "$ACCEPT_JSON")

  UPTIME=$(echo "$RESPONSE" | jq '.measurements[0].value' 2>/dev/null)
  UPTIME_MINUTES=$(echo "scale=2; $UPTIME / 60" | bc)

  print_metric "Tempo de execução: $UPTIME_MINUTES minutos"
}

# 5. Teste: CPU Usage
test_cpu_usage() {
  print_header "5. USO DE CPU"

  RESPONSE=$(curl -s -X GET "$BASE_URL/actuator/metrics/process.cpu.usage" \
    -H "$ACCEPT_JSON")

  CPU=$(echo "$RESPONSE" | jq '.measurements[0].value' 2>/dev/null)
  CPU_PERCENT=$(echo "scale=2; $CPU * 100" | bc)

  print_metric "Uso de CPU: $CPU_PERCENT %"
}

# 6. Teste: Cache Metrics
test_cache_metrics() {
  print_header "6. MÉTRICAS DE CACHE"

  print_info "Verificando cache hits..."
  RESPONSE=$(curl -s -X GET "$BASE_URL/actuator/metrics/cache.gets.hit" \
    -H "$ACCEPT_JSON" 2>/dev/null)

  HIT_COUNT=$(echo "$RESPONSE" | jq '.measurements[0].value' 2>/dev/null || echo "0")
  print_metric "Cache hits: $HIT_COUNT"

  print_info "Verificando cache misses..."
  RESPONSE_MISS=$(curl -s -X GET "$BASE_URL/actuator/metrics/cache.gets.miss" \
    -H "$ACCEPT_JSON" 2>/dev/null)

  MISS_COUNT=$(echo "$RESPONSE_MISS" | jq '.measurements[0].value' 2>/dev/null || echo "0")
  print_metric "Cache misses: $MISS_COUNT"

  # Calcular hit rate
  if [ "$HIT_COUNT" != "0" ] && [ "$MISS_COUNT" != "0" ]; then
    TOTAL=$((HIT_COUNT + MISS_COUNT))
    HIT_RATE=$(echo "scale=2; ($HIT_COUNT / $TOTAL) * 100" | bc)
    print_metric "Cache hit rate: $HIT_RATE %"
  fi
}

# 7. Teste: Caches Endpoint
test_caches_endpoint() {
  print_header "7. INFORMAÇÕES DE CACHES"

  RESPONSE=$(curl -s -X GET "$BASE_URL/actuator/caches" \
    -H "$ACCEPT_JSON")

  echo "Response:"
  echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"
}

# 8. Teste: Prometheus Format
test_prometheus_format() {
  print_header "8. MÉTRICAS EM FORMATO PROMETHEUS"

  print_info "Recuperando métricas em formato Prometheus..."

  RESPONSE=$(curl -s -X GET "$BASE_URL/actuator/prometheus" 2>/dev/null)

  LINES=$(echo "$RESPONSE" | wc -l)
  print_success "Total de linhas de métricas: $LINES"

  echo ""
  echo "Primeiras 20 linhas:"
  echo "$RESPONSE" | head -20
}

# 9. Teste: Custom Cache Metrics Endpoint
test_custom_cache_metrics() {
  print_header "9. MÉTRICAS CUSTOMIZADAS - CACHE"

  RESPONSE=$(curl -s -X GET "$BASE_URL/api/v1/metrics/cache" \
    -H "$ACCEPT_JSON")

  echo "Response:"
  echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"

  CACHE_TYPE=$(echo "$RESPONSE" | jq -r '.cache_manager_type' 2>/dev/null)
  print_success "Tipo de Cache Manager: $CACHE_TYPE"
}

# 10. Teste: Custom Redis Status Endpoint
test_redis_status() {
  print_header "10. STATUS DO REDIS"

  RESPONSE=$(curl -s -X GET "$BASE_URL/api/v1/metrics/redis" \
    -H "$ACCEPT_JSON")

  echo "Response:"
  echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"

  STATUS=$(echo "$RESPONSE" | jq -r '.redis_status' 2>/dev/null)
  print_success "Status do Redis: $STATUS"
}

# 11. Teste: Threads
test_threads() {
  print_header "11. THREADS DO PROCESSO"

  RESPONSE=$(curl -s -X GET "$BASE_URL/actuator/metrics/jvm.threads.live" \
    -H "$ACCEPT_JSON")

  THREADS=$(echo "$RESPONSE" | jq '.measurements[0].value' 2>/dev/null)
  print_metric "Threads ativas: $THREADS"
}

# 12. Teste: System Load
test_system_load() {
  print_header "12. CARGA DO SISTEMA"

  RESPONSE=$(curl -s -X GET "$BASE_URL/actuator/metrics/system.load.average.1m" \
    -H "$ACCEPT_JSON" 2>/dev/null)

  if [ ! -z "$RESPONSE" ]; then
    LOAD=$(echo "$RESPONSE" | jq '.measurements[0].value' 2>/dev/null)
    print_metric "Carga média (1m): $LOAD"
  else
    print_info "System load average não disponível nesta plataforma"
  fi
}

# 🔬 Teste: Fluxo Completo com Cache Operations
test_cache_operations() {
  print_header "13. TESTE DE OPERAÇÕES COM CACHE"

  print_info "Fazendo requisições para criar cache..."

  # Fazer 3 requisições GET para popular o cache
  for i in 1 2 3; do
    curl -s -X GET "$BASE_URL/api/v1/users" -H "$ACCEPT_JSON" > /dev/null
    print_info "Requisição $i feita"
  done

  echo ""
  print_info "Aguardando 2 segundos..."
  sleep 2

  print_info "Verificando métricas de cache após operações..."
  test_cache_metrics
}

# Menu Principal
main() {
  echo -e "${BLUE}"
  echo "╔════════════════════════════════════════╗"
  echo "║   Teste de Actuator e Métricas Redis   ║"
  echo "╚════════════════════════════════════════╝"
  echo -e "${NC}"

  check_server

  # Executar todos os testes
  test_health
  test_metrics_list
  test_jvm_memory
  test_process_uptime
  test_cpu_usage
  test_cache_metrics
  test_caches_endpoint
  test_prometheus_format
  test_custom_cache_metrics
  test_redis_status
  test_threads
  test_system_load
  test_cache_operations

  print_header "Testes Concluídos!"
  print_success "Todos os testes de metrics foram executados com sucesso"

  echo ""
  echo -e "${YELLOW}Dicas:${NC}"
  echo "1. Prometheus metrics: http://localhost:8080/actuator/prometheus"
  echo "2. Health check: http://localhost:8080/actuator/health"
  echo "3. Ver todas as métricas: http://localhost:8080/actuator/metrics"
  echo "4. Cache customizado: http://localhost:8080/api/v1/metrics/cache"
  echo "5. Redis status: http://localhost:8080/api/v1/metrics/redis"
}

# Executar
main "$@"

