# 🚀 Guia Rápido - Actuator e Métricas do Redis

## Tudo Configurado! ✅

Seu projeto foi configurado com **Spring Boot Actuator** e **Métricas de Redis**. Aqui está como usar:

---

## ⚡ Começar Rapidamente

### 1. Iniciar o Servidor
```bash
mvn spring-boot:run
```

### 2. Verificar Health (30 segundos)
```bash
curl -X GET http://localhost:8080/actuator/health
```

**Resposta esperada:**
```json
{
  "status": "UP",
  "components": {...}
}
```

### 3. Ver Todas as Métricas
```bash
curl -X GET http://localhost:8080/actuator/metrics
```

---

## 📊 Endpoints Principais

| Endpoint | Descrição | Uso |
|----------|-----------|-----|
| `/actuator/health` | Status da aplicação | Verificação rápida |
| `/api/v1/metrics/redis` | Lista de métricas | Descoberta |
| `/actuator/prometheus` | Formato Prometheus | Integração |
| `/actuator/caches` | Info de caches | Debugging |
| `/api/v1/metrics/cache` | Cache customizado | Monitoramento |
| `/api/v1/metrics/redis` | Status Redis | Diagnóstico |

---

## 🧪 Testes Rápidos

### Teste Tudo com Script Bash
```bash
bash actuator_metrics_test.sh
```

Isso vai testar:
- ✅ Health Check
- ✅ Métricas JVM
- ✅ CPU e Memória
- ✅ Cache Statistics
- ✅ Redis Status
- ✅ E mais 10 testes

---

### Teste no IntelliJ IDEA
1. Abrir: `requests_actuator.http`
2. Clicar no ▶️ verde ao lado de cada requisição
3. Ver resultado na aba "REST Client"

---

## 📈 Monitorar em Tempo Real

### Memória JVM
```bash
watch -n 2 'curl -s http://localhost:8080/actuator/metrics/jvm.memory.used | jq ".measurements[0].value"'
```

### Cache Hit Rate
```bash
curl -s http://localhost:8080/actuator/metrics/cache.gets.hit | jq ".measurements[0].value"
```

### Prometheus All
```bash
curl -s http://localhost:8080/actuator/prometheus | head -50
```

---

## 📁 Arquivos Criados

| Arquivo | Descrição |
|---------|-----------|
| `pom.xml` | ✅ Dependências adicionadas (Actuator, Micrometer) |
| `application.properties` | ✅ Configuração Actuator |
| `application-local.properties` | ✅ Profile para local sem Redis |
| `ActuatorAndMetricsConfig.java` | ✅ Classe de configuração |
| `CacheMetricsController.java` | ✅ Controller com endpoints customizados |
| `ACTUATOR_REDIS_METRICS_GUIDE.md` | 📖 Documentação completa |
| `requests_actuator.http` | 📄 Requisições prontas |
| `actuator_metrics_test.sh` | 🐚 Script de testes |

---

## 🔍 Exemplos de Uso

### 1. Ver Memória Usada
```bash
curl http://localhost:8080/actuator/metrics/jvm.memory.used | jq .
```

### 2. Ver Status Redis
```bash
curl http://localhost:8080/api/v1/metrics/redis | jq .
```

### 3. Ver Cache Customizadas
```bash
curl http://localhost:8080/api/v1/metrics/cache | jq .
```

### 4. Exportar para Prometheus
```bash
curl http://localhost:8080/actuator/prometheus > metrics.txt
```

---

## 🎯 Próximos Passos

1. ✅ Iniciar servidor: `mvn spring-boot:run`
2. ✅ Testar health: `curl http://localhost:8080/actuator/health`
3. ✅ Executar script: `bash actuator_metrics_test.sh`
4. ✅ Usar IntelliJ: Abrir `requests_actuator.http`
5. ✅ Integrar Prometheus (opcional)

---

## 📚 Leitura Completa

Para detalhes completos, ver: **ACTUATOR_REDIS_METRICS_GUIDE.md**

---

## 🆘 Troubleshooting

**Erro: Redis não conecta?**
```bash
# Usar profile local sem Redis
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"
```

**Métricas de cache vazias?**
1. Fazer algumas requisições na API primeiro
2. Esperar 2-3 segundos
3. Consultar as métricas novamente

**Actuator não responde?**
1. Verificar se porta 8080 está disponível
2. Ver logs: `mvn spring-boot:run`

---

**Versão:** 1.0
**Data:** 09 de junho de 2026
**Status:** ✅ Pronto para Usar

