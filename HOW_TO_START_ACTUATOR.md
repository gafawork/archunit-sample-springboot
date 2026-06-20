# 🎯 COMEÇAR A USAR ACTUATOR E MÉTRICAS DO REDIS

## ✅ TUDO PRONTO! Aqui está como começar...

---

## 🚀 Passo 1: Iniciar a Aplicação

### No Terminal (PowerShell/Bash)
```bash
mvn spring-boot:run
```

**Saída esperada:**
```
2026-06-09 10:30:00.000  INFO 12345 --- [main] c.e.u.UserManagementApiApplication     : Starting UserManagementApiApplication
2026-06-09 10:30:05.000  INFO 12345 --- [main] c.e.u.UserManagementApiApplication     : Started UserManagementApiApplication
2026-06-09 10:30:05.000  INFO 12345 --- [main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080
```

Aguarde até ver: **"Tomcat started on port(s): 8080"**

---

## 🧪 Passo 2: Testar Endpoints do Actuator

### Opção A: Script Automático (Recomendado)

**Em outro terminal:**
```bash
bash actuator_metrics_test.sh
```

**Resultado esperado:**
```
✅ Servidor está respondendo em http://localhost:8080
✓ Aplicação está HEALTHY
✓ Total de métricas disponíveis: 45
📊 Memória JVM usada: 150.32 MB
...
✓ Todos os testes de metrics foram executados com sucesso
```

---

### Opção B: Teste Manual com cURL

```bash
# 1. Verificar saúde
curl http://localhost:8080/actuator/health

# 2. Ver métricas disponíveis
curl http://localhost:8080/actuator/metrics

# 3. Ver memória JVM
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# 4. Ver status Redis
curl http://localhost:8080/api/v1/metrics/redis

# 5. Ver cache customizado
curl http://localhost:8080/api/v1/metrics/cache
```

---

### Opção C: Usar IntelliJ IDEA REST Client

1. Abrir arquivo: **`requests_actuator.http`**
2. Clicar no ícone ▶️ verde antes de cada requisição
3. Ver resultado na aba "REST Client" à direita

Exemplo:
```http
GET http://localhost:8080/actuator/health
Accept: application/json
```

---

## 📊 Verificar Funcionamento

### 1. Health Check (Deve retornar UP)
```bash
curl http://localhost:8080/actuator/health | jq '.status'
```

**Resposta esperada:** `"UP"`

---

### 2. Contar Métricas (Deve retornar >40)
```bash
curl http://localhost:8080/actuator/metrics | jq '.names | length'
```

**Resposta esperada:** `45` (ou similar)

---

### 3. Memória JVM (Deve retornar valor em bytes)
```bash
curl http://localhost:8080/actuator/metrics/jvm.memory.used | jq '.measurements[0].value'
```

**Resposta esperada:** `123456789` (número grande)

---

### 4. Cache Customizado (Deve retornar tipo de cache)
```bash
curl http://localhost:8080/api/v1/metrics/cache | jq '.cache_manager_type'
```

**Resposta esperada:** `"RedisCacheManager"` ou `"SimpleCacheManager"` (se local)

---

## 🔍 Explorar Métricas

### Ver Todos os Nomes de Métricas
```bash
curl -s http://localhost:8080/actuator/metrics | jq '.names | sort'
```

### Ver CPU Usage
```bash
curl -s http://localhost:8080/actuator/metrics/process.cpu.usage | jq '.measurements[0].value'
```

### Ver Uptime
```bash
curl -s http://localhost:8080/actuator/metrics/process.uptime | jq '.measurements[0].value'
```

### Ver Formato Prometheus
```bash
curl -s http://localhost:8080/actuator/prometheus | head -50
```

---

## 💾 Gerar Cache e Ver Métricas

### 1. Criar um Usuário (vai gerar cache)
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "email":"test@example.com",
    "firstName":"Test",
    "lastName":"User",
    "password":"Password123"
  }'
```

### 2. Listar Usuários (vai usar cache)
```bash
curl http://localhost:8080/api/v1/users
```

### 3. Verificar Cache Hits
```bash
curl -s http://localhost:8080/actuator/metrics/cache.gets.hit | jq '.measurements[0].value'
```

---

## 📈 Monitorar em Tempo Real

### Monitorar Memória (Linux/Mac)
```bash
watch -n 2 'curl -s http://localhost:8080/actuator/metrics/jvm.memory.used | jq ".measurements[0].value"'
```

### Monitorar CPU
```bash
watch -n 2 'curl -s http://localhost:8080/actuator/metrics/process.cpu.usage | jq ".measurements[0].value"'
```

---

## 📊 Integrar com Prometheus (Opcional)

### 1. Criar arquivo prometheus.yml
```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'user-api'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/actuator/prometheus'
```

### 2. Iniciar Prometheus
```bash
# Assumindo que Prometheus está instalado
prometheus --config.file=prometheus.yml
```

### 3. Acessar Prometheus
```
http://localhost:9090
```

---

## 🛑 Parar a Aplicação

**No terminal onde Maven está rodando:**
```
Ctrl + C
```

Esperado:
```
2026-06-09 10:35:00.000  INFO 12345 --- [shutdown-hook] c.e.u.UserManagementApiApplication   : Closing connection
```

---

## 🆘 Troubleshooting

### Erro: "Connection refused"
```
Solução: Verificar se servidor está rodando
Comando: curl http://localhost:8080/actuator/health
```

### Erro: "Port 8080 already in use"
```bash
# Encontrar o processo usando porta 8080
lsof -i :8080 (Linux/Mac)
netstat -ano | findstr :8080 (Windows)

# Matar o processo
kill -9 <PID> (Linux/Mac)
taskkill /PID <PID> /F (Windows)
```

### Redis não conecta
```bash
# Usar perfil local (cache em memória, sem Redis)
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"
```

---

## 📚 Documentação Disponível

| Arquivo | Descrição | Tempo |
|---------|-----------|-------|
| ACTUATOR_QUICK_START.md | Início rápido | 5 min |
| ACTUATOR_REDIS_METRICS_GUIDE.md | Guia completo | 30 min |
| ACTUATOR_CONFIG_COMPLETE.md | Status final | 10 min |
| requests_actuator.http | Exemplos HTTP | Imediato |
| actuator_metrics_test.sh | Script de testes | 5 min |

---

## ✅ Checklist de Funcionamento

- [ ] Servidor iniciou sem erros
- [ ] Health check retorna "UP"
- [ ] Métricas retornam >40 items
- [ ] Memória JVM é um número grande
- [ ] Cache funciona (GET melhor que POST)
- [ ] Redis status está "connected" (ou "ok" em local)
- [ ] Prometheus format retorna linhas
- [ ] Script de testes passa em todos

---

## 🎯 Próximas Coisas Para Fazer

1. ✅ Leia: `ACTUATOR_QUICK_START.md`
2. ✅ Teste: `bash actuator_metrics_test.sh`
3. ✅ Explore: Endpoints HTTP em `requests_actuator.http`
4. ✅ Integre: Com Prometheus e Grafana (opcional)
5. ✅ Customize: Adicione suas próprias métricas

---

## 📞 Precisa de Ajuda?

- **Erro de compilação?** → Verifique `ACTUATOR_CONFIG_COMPLETE.md`
- **Endpoint não funciona?** → Veja `ACTUATOR_REDIS_METRICS_GUIDE.md`
- **Quer exemplo?** → Abra `requests_actuator.http`
- **Error 404?** → Verifique se servidor está rodando

---

## 🎉 VOCÊ ESTÁ PRONTO!

```bash
# Abra dois terminais:

# Terminal 1: Rodar servidor
mvn spring-boot:run

# Terminal 2: Testar (depois de 10 segundos)
bash actuator_metrics_test.sh
```

**Enjoy! Seu Actuator e Métricas estão funcionando! 🚀**

---

**Data:** 09 de junho de 2026
**Versão:** 1.0
**Status:** ✅ Pronto para uso

