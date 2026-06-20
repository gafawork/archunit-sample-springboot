# 📊 Configuração do Actuator e Métricas do Redis

## Visão Geral

O projeto foi configurado com **Spring Boot Actuator** e **Prometheus** para monitorar métricas de cache do Redis, incluindo:

- ✅ Health checks (saúde da aplicação)
- ✅ Métricas de JVM (memória, CPU, GC)
- ✅ Métricas de processo (threads, uptime)
- ✅ Métricas de cache (hits, misses, evictions)
- ✅ Métricas do sistema operacional
- ✅ Exportação em formato Prometheus

---

## 🚀 Iniciar a Aplicação

### Com Redis (Production)
```bash
mvn spring-boot:run
```

Base de dados: Redis (localhost:6379)
Cache: Redis
Atuador: Ativado com métricas Redis

### Com Cache em Memória (Development)
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"
```

Ou no IDE, definir variável de environment:
```
SPRING_PROFILES_ACTIVE=local
```

Base de dados: H2 (em memória)
Cache: Simple (memória local)
Atuador: Ativado com métricas de cache em memória

---

## 🔍 Endpoints do Actuator

### 1. Health Check
**Endpoint:** `GET /actuator/health`

**Resposta 200 OK:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "H2",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 536870912,
        "free": 268435456,
        "threshold": 10485760,
        "status": "UP"
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

**Como testar:**
```bash
curl -X GET http://localhost:8080/actuator/health
```

---

### 2. Lista de Métricas Disponíveis
**Endpoint:** `GET /actuator/metrics`

**Resposta 200 OK:**
```json
{
  "names": [
    "jvm.memory.used",
    "jvm.memory.max",
    "jvm.memory.committed",
    "jvm.threads.live",
    "jvm.threads.peak",
    "jvm.classes.loaded",
    "jvm.gc.memory.allocated",
    "jvm.gc.max.data.size",
    "process.cpu.usage",
    "process.uptime",
    "system.load.average.1m",
    "cache.gets",
    "cache.puts",
    "cache.evictions",
    "cache.removals"
  ]
}
```

**Como testar:**
```bash
curl -X GET http://localhost:8080/actuator/metrics
```

---

### 3. Métrica Específica - JVM Memory
**Endpoint:** `GET /actuator/metrics/jvm.memory.used`

**Resposta 200 OK:**
```json
{
  "name": "jvm.memory.used",
  "description": "The amount of used memory",
  "baseUnit": "bytes",
  "measurements": [
    {
      "statistic": "VALUE",
      "value": 123456789
    }
  ],
  "availableTags": [
    {
      "tag": "area",
      "values": ["heap", "nonheap"]
    },
    {
      "tag": "id",
      "values": ["PS Survivor Space", "PS Old Generation", "PS Eden Space"]
    }
  ]
}
```

**Exemplos com filtros:**
```bash
# Memória total usada
curl -X GET http://localhost:8080/actuator/metrics/jvm.memory.used

# Apenas heap
curl -X GET "http://localhost:8080/actuator/metrics/jvm.memory.used?tag=area:heap"
```

---

### 4. Métricas de Cache
**Endpoint:** `GET /actuator/metrics/cache.gets`

**Tipos de métricas de cache:**
```
cache.gets          - Total de gets (hits + misses)
cache.gets.hit      - Total de cache hits
cache.gets.miss     - Total de cache misses
cache.puts          - Total de operações de put
cache.removals      - Total de remoções do cache
cache.evictions     - Total de evictions
cache.size          - Tamanho do cache
```

**Como testar:**
```bash
curl -X GET http://localhost:8080/actuator/metrics/cache.gets.hit
curl -X GET http://localhost:8080/actuator/metrics/cache.gets.miss
```

---

### 5. Metrics em Formato Prometheus
**Endpoint:** `GET /actuator/prometheus`

**Resposta** (formato Prometheus):
```
# HELP jvm_memory_used_bytes The amount of used memory
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{application="user-management-api",area="heap",id="PS Survivor Space",} 123456789.0

# HELP process_uptime_seconds The uptime of the process
# TYPE process_uptime_seconds gauge
process_uptime_seconds{application="user-management-api",} 3600.0

# HELP cache_gets_total 
# TYPE cache_gets_total counter
cache_gets_total{application="user-management-api",cache="users",result="hit",} 150.0
cache_gets_total{application="user-management-api",cache="users",result="miss",} 50.0
```

**Como testar:**
```bash
curl -X GET http://localhost:8080/actuator/prometheus
```

---

### 6. Informações de Caches
**Endpoint:** `GET /actuator/caches`

**Resposta 200 OK:**
```json
{
  "cacheManagers": [
    {
      "name": "cacheManager",
      "caches": [
        {
          "name": "users",
          "target": "org.springframework.data.redis.cache.DefaultRedisCacheWriter"
        },
        {
          "name": "usersList",
          "target": "org.springframework.data.redis.cache.DefaultRedisCacheWriter"
        }
      ]
    }
  ]
}
```

**Como testar:**
```bash
curl -X GET http://localhost:8080/actuator/caches
```

---

## 🎯 Endpoints Customizados Criados

### 1. Métricas de Cache Customizadas
**Endpoint:** `GET /api/v1/metrics/cache`

**Resposta 200 OK:**
```json
{
  "cache_manager_type": "RedisCacheManager",
  "cache_names": ["users", "usersList"],
  "metrics": {
    "total_metrics": 129,
    "cache_config": {
      "cache": true,
      "redis_enabled": true
    }
  }
}
```

**Como testar:**
```bash
curl -X GET http://localhost:8080/api/v1/metrics/cache
```

---

### 2. Status do Redis
**Endpoint:** `GET /api/v1/metrics/redis`

**Resposta 200 OK:**
```json
{
  "redis_status": "connected",
  "cache_type": "RedisCacheManager",
  "configuration": {
    "host": "localhost",
    "port": "6379",
    "timeout": "600000"
  },
  "available_caches": ["users", "usersList"]
}
```

**Como testar:**
```bash
curl -X GET http://localhost:8080/api/v1/metrics/redis
```

---

## 📈 Monitorar em Tempo Real

### Usando cURL com Watch (Linux/Mac)
```bash
# Atualizar a cada 2 segundos
watch -n 2 'curl -s http://localhost:8080/actuator/health | jq .'
```

### Usando jq para Filtrar
```bash
# Ver apenas o status
curl -s http://localhost:8080/actuator/health | jq '.status'

# Ver memória JVM
curl -s http://localhost:8080/actuator/metrics/jvm.memory.used | jq '.measurements[0].value'

# Ver cache hits
curl -s "http://localhost:8080/actuator/metrics/cache.gets.hit" | jq '.measurements[0].value'
```

---

## 🔧 Configurações Aplicadas

### application.properties (Principal)
```properties
# Actuator Configuration
management.endpoints.web.exposure.include=health,metrics,prometheus,caches
management.endpoint.health.show-details=when-authorized

# Metrics Configuration
management.metrics.enable.jvm=true
management.metrics.enable.process=true
management.metrics.enable.system=true
management.metrics.enable.cache=true
management.metrics.enable.logback=true

# Prometheus Configuration
management.prometheus.metrics.export.enabled=true
```

### application-local.properties
```properties
# Cache em memória (sem Redis)
spring.cache.type=simple
# Pré-declara os caches para que sejam registrados nas métricas no startup
spring.cache.cache-names=users,usersList

# Mesmas configurações de Actuator
management.endpoints.web.exposure.include=health,metrics,prometheus,caches
```

---

## 📦 Dependências Adicionadas

### pom.xml
```xml
<!-- Spring Boot Actuator -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<!-- Prometheus Metrics -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

---

## 🏗️ Classes de Configuração Criadas

### CacheConfig.java
Configuração central do cache e das métricas do Redis. Pontos-chave para que as
métricas sejam **realmente coletadas** e o cache funcione no hit:

- `@EnableCaching` para ativar o suporte a cache.
- `RedisCacheManagerBuilderCustomizer` com `enableStatistics()` — **obrigatório**
  para que o Micrometer colete `cache.gets` (hit/miss), `cache.puts` e
  `cache.removals` (o `RedisCache` usa `CacheStatisticsCollector.none()` por padrão).
- Serializer genérico com **default typing** (grava `@class`) para o cache `users`
  (objeto único), evitando que o valor volte como `LinkedHashMap`. O typing é
  restringido por um `PolymorphicTypeValidator` aos pacotes da aplicação.
- Configuração **dedicada para `usersList`** com serializer **tipado**
  `List<UserResponse>`: o default typing com `As.PROPERTY` não grava o type-id do
  container na raiz, o que quebra o round-trip de coleções; o serializer tipado
  conhece o `JavaType` e dispensa o type-id.

```java
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String USERS_CACHE = "users";
    public static final String USERS_LIST_CACHE = "usersList";
    private static final Duration TTL = Duration.ofMinutes(10);

    @Bean
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = true)
    public RedisCacheConfiguration redisCacheConfiguration() {
        return baseConfiguration()
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(GenericJacksonJsonRedisSerializer.builder()
                    .enableDefaultTyping(redisTypeValidator())
                    .build()));
    }

    @Bean
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = true)
    public RedisCacheManagerBuilderCustomizer redisCacheManagerCustomizer() {
        return builder -> builder
            .enableStatistics()
            .withCacheConfiguration(USERS_LIST_CACHE, usersListCacheConfiguration());
    }

    private static RedisCacheConfiguration usersListCacheConfiguration() {
        JavaType listType = TypeFactory.createDefaultInstance()
            .constructCollectionType(List.class, UserResponse.class);
        return baseConfiguration()
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new JacksonJsonRedisSerializer<>(listType)));
    }

    private static RedisCacheConfiguration baseConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(TTL)
            .disableCachingNullValues();
    }

    private static PolymorphicTypeValidator redisTypeValidator() {
        return BasicPolymorphicTypeValidator.builder()
            .allowIfSubType("com.example.")
            .allowIfSubType("java.util.")
            .build();
    }
}
```

> As tags comuns (ex.: `application`) são definidas via propriedade
> `management.metrics.tags.application` em `application.properties`, não por um
> `MeterRegistryCustomizer`.

### CacheMetricsController.java
Endpoints REST customizados:
- `GET /api/v1/metrics/cache` - Métricas do cache
- `GET /api/v1/metrics/redis` - Status do Redis

---

## 🧪 Cenários de Uso

### Cenário 1: Verificar Saúde da Aplicação
```bash
# Health check básico
curl -X GET http://localhost:8080/actuator/health

# Resposta esperada: "status": "UP"
```

### Cenário 2: Monitorar Uso de Memória
```bash
# Ver memória heap usada
curl -X GET "http://localhost:8080/actuator/metrics/jvm.memory.used?tag=area:heap" | jq '.measurements[0].value'

# Resultado: valor em bytes (ex: 123456789)
```

### Cenário 3: Verificar Cache Hit Rate
```bash
# Ver cache hits
curl -s "http://localhost:8080/actuator/metrics/cache.gets.hit?tag=cache:users" | jq '.measurements[0].value'

# Ver cache misses
curl -s "http://localhost:8080/actuator/metrics/cache.gets.miss?tag=cache:users" | jq '.measurements[0].value'

# Calcular hit rate: hits / (hits + misses)
```

### Cenário 4: Exportar Métricas para Prometheus
```bash
# Coletar todas as métricas em formato Prometheus
curl -X GET http://localhost:8080/actuator/prometheus > metrics.txt

# Usar em arquivo prometheus.yml:
# scrape_configs:
#   - job_name: 'user-api'
#     static_configs:
#       - targets: ['localhost:8080']
#     metrics_path: '/actuator/prometheus'
```

### Cenário 5: Listar Todas as Métricas Disponíveis
```bash
# Ver lista completa
curl -s http://localhost:8080/actuator/metrics | jq '.names | sort'

# Resultado: array com nomes de todas as métricas
```

---

## 📊 Integrando com Prometheus e Grafana

### 1. Prometheus (prometheus.yml)
```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'user-management-api'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/actuator/prometheus'
```

### 2. Grafana Dashboard
Exemplos de queries para Grafana:

**Cache Hit Rate:**
```promql
rate(cache_gets_hit[5m]) / (rate(cache_gets_hit[5m]) + rate(cache_gets_miss[5m]))
```

**Memory Usage:**
```promql
jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}
```

**CPU Usage:**
```promql
process_cpu_usage
```

---

## 🔒 Segurança do Actuator

### Endpoints Sensíveis
Por padrão, alguns endpoints estão habilitados:
- ✅ `/actuator/health` - Sempre público
- ✅ `/actuator/metrics` - Métrica de leitura
- ✅ `/actuator/prometheus` - Formato Prometheus

### Recomendações para Produção
1. **Restringir acesso:**
```properties
management.endpoints.web.exposure.include=health,metrics,prometheus
management.endpoints.web.exposure.exclude=shutdown
```

2. **Require authentication:**
```properties
management.endpoint.health.show-details=when-authorized
```

3. **Usar Spring Security:**
```java
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .requestMatchers("/actuator/**").hasRole("ADMIN")
            .anyRequest().permitAll();
        return http.build();
    }
}
```

---

## 🐛 Troubleshooting

### Problema: Redis não está conectado
**Solução:**
```bash
# Verificar se Redis está rodando
redis-cli ping
# Resposta: PONG

# Se Redis não estiver disponível, usar o profile local
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"
```

### Problema: Métricas de cache vazias
**Solução:**
1. Fazer pelo menos uma requisição na API (para criar entradas em cache)
2. Esperar alguns segundos
3. Consultar `/actuator/metrics/cache.gets`

### Problema: Prometheus não consegue scrape
**Verificar:**
1. URL correta: `http://localhost:8080/actuator/prometheus`
2. Aplicação está rodando
3. Portas não bloqueadas por firewall

---

## 📝 Testando as Métricas

### Script de Teste (api_metrics_test.sh)
```bash
#!/bin/bash

echo "=== Testing Actuator Metrics ==="

echo "1. Health Check"
curl -s http://localhost:8080/actuator/health | jq '.status'

echo "2. Available Metrics"
curl -s http://localhost:8080/actuator/metrics | jq '.names | length'

echo "3. JVM Memory Used"
curl -s http://localhost:8080/actuator/metrics/jvm.memory.used | jq '.measurements[0].value'

echo "4. Cache Status"
curl -s http://localhost:8080/api/v1/metrics/cache | jq '.cache_manager_type'

echo "5. Redis Status"
curl -s http://localhost:8080/api/v1/metrics/redis | jq '.redis_status'
```

---

## 📚 Referências

- [Spring Boot Actuator Documentation](https://spring.io/guides/gs/actuator-service/)
- [Micrometer Prometheus Registry](https://micrometer.io/docs/registry/prometheus)
- [Spring Data Redis Metrics](https://spring.io/projects/spring-data-redis)
- [Prometheus Querying](https://prometheus.io/docs/prometheus/latest/querying/basics/)

---

## 🎯 Próximos Passos

1. ✅ Iniciar aplicação: `mvn spring-boot:run`
2. ✅ Verificar health: `curl http://localhost:8080/actuator/health`
3. ✅ Ver métricas: `curl http://localhost:8080/actuator/metrics`
4. ✅ Exportar Prometheus: `curl http://localhost:8080/actuator/prometheus`
5. ✅ Configurar Grafana para visualização (opcional)

---

**Data de Criação:** 09 de junho de 2026
**Versão:** 1.0
**Status:** ✅ Pronto para Usar

