# 📝 REFERÊNCIA RÁPIDA - ACTUATOR E REDIS METRICS

## ⚡ 30 segundos para começar

```bash
# Terminal 1: Iniciar servidor
mvn spring-boot:run

# Terminal 2: Testar (depois de 10 segundos)
bash actuator_metrics_test.sh
```

---

## 🔗 URLs de Teste Rápido

| URL | O que faz |
|-----|-----------|
| `curl http://localhost:8080/actuator/health` | Verificar saúde |
| `curl http://localhost:8080/actuator/metrics` | Listar métricas |
| `curl http://localhost:8080/api/v1/metrics/redis` | Status Redis |
| `curl http://localhost:8080/api/v1/metrics/cache` | Info de cache |
| `curl http://localhost:8080/actuator/prometheus` | Formato Prometheus |

---

## 📁 Arquivos Criados

```
✅ ACTUATOR_QUICK_START.md ............... COMECE AQUI (5 min)
✅ HOW_TO_START_ACTUATOR.md ............ Instruções detalhadas
✅ ACTUATOR_REDIS_METRICS_GUIDE.md .... Guia completo (30 min)
✅ ACTUATOR_CONFIG_COMPLETE.md ....... Status final
✅ requests_actuator.http .............. 15+ requisições prontas
✅ actuator_metrics_test.sh ........... Script com 13 testes
✅ ActuatorAndMetricsConfig.java ...... Classe de config
✅ CacheMetricsController.java ........ Controller customizado
```

---

## 📊 Métricas Disponíveis

| Métrica | URL |
|---------|-----|
| Memória JVM | `/actuator/metrics/jvm.memory.used` |
| CPU | `/actuator/metrics/process.cpu.usage` |
| Uptime | `/actuator/metrics/process.uptime` |
| Threads | `/actuator/metrics/jvm.threads.live` |
| Cache Hits | `/actuator/metrics/cache.gets.hit` |
| Cache Misses | `actuator/metrics/cache.gets.hit` |

---

## 🐚 Comandos Úteis

```bash
# Ver saúde
curl http://localhost:8080/actuator/health | jq '.status'

# Contar métricas
curl http://localhost:8080/actuator/metrics | jq '.names | length'

# Memória em MB
curl -s http://localhost:8080/actuator/metrics/jvm.memory.used | \
  jq '.measurements[0].value / 1048576'

# Executar todos os testes
bash actuator_metrics_test.sh

# Monitorar em tempo real (Linux)
watch -n 2 'curl -s http://localhost:8080/actuator/health | jq ".status"'
```

---

## ✅ Checklist

- [ ] Compilou sem erros
- [ ] Servidor iniciou
- [ ] Health retorna "UP"
- [ ] Metrics lista >40 items
- [ ] Cache funciona
- [ ] Redis conectado

---

## 🆘 Se Não Funcionar

```bash
# Sem Redis
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"

# Limpar e recompilar
mvn clean compile

# Porta já está em uso
lsof -i :8080  # para identificar
kill -9 <PID>  # para matar
```

---

**Date:** 09 de junho de 2026 | **Status:** ✅ Pronto

