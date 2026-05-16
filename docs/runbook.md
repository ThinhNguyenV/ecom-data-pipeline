# Runbook — ecom-data-pipeline

## Starting the Stack

```bash
# Full stack
make docker-up

# Verify all healthy
docker compose -f deploy/docker/docker-compose.yml ps
```

Expected healthy services: `postgres`, `elasticsearch`, `kafka`, `zookeeper`, `backend`

---

## Common Operations

### Seed data into PostgreSQL
```bash
# Automatic on first docker compose up (init.sql volume mount)
# Manual re-seed:
docker exec -i postgres psql -U user -d ecom_dw < deploy/sql/init.sql
```

### Run dbt models
```bash
make dbt-run         # runs all models
make dbt-test        # runs schema + custom tests
cd dbt && dbt run --select staging  # run only staging models
```

### Start Kafka producer
```bash
make kafka-producer
# Or directly:
PYTHONPATH=src python -m ecom_pipeline.ingestion.producer
```

### Check Elasticsearch indices
```bash
curl http://localhost:9200/_cat/indices?v
curl http://localhost:9200/products/_count
```

### Check API health
```bash
curl http://localhost:8090/actuator/health
```

---

## Troubleshooting

### PostgreSQL connection refused
```bash
docker logs postgres
docker exec postgres pg_isready -U user -d ecom_dw
```

### Kafka consumer lag
```bash
docker exec kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --group ecom_consumer_group \
  --describe
```

### dbt model failures
```bash
cd dbt
dbt debug --profiles-dir .     # test connection
dbt compile --profiles-dir .   # check SQL compilation
```

### Backend won't start
```bash
docker logs ecom-backend
# Common causes:
# - PostgreSQL not ready (check depends_on healthcheck)
# - Wrong SPRING_DATASOURCE_URL
# - Elasticsearch unreachable
```

### Spring Boot remote debug (dev mode)
```bash
# Port 5005 is exposed in docker-compose.override.yml
# In IntelliJ: Run > Edit Configurations > Remote JVM Debug > localhost:5005
```

---

## Monitoring

| Endpoint | Purpose |
|---|---|
| `http://localhost:8090/actuator/health` | App health |
| `http://localhost:8090/actuator/metrics` | JVM metrics |
| `http://localhost:8080` | Spark master UI |
| `http://localhost:8081` | Airflow UI |
| `http://localhost:5601` | Kibana |
| `http://localhost:9200/_cluster/health` | ES cluster health |
