#!/usr/bin/env bash
# =============================================================
# run_pipeline.sh — Run the full end-to-end pipeline manually
# Usage: bash scripts/run_pipeline.sh [--skip-producer] [--skip-dbt]
# =============================================================
set -euo pipefail

SKIP_PRODUCER=false
SKIP_DBT=false

for arg in "$@"; do
    case $arg in
        --skip-producer) SKIP_PRODUCER=true ;;
        --skip-dbt)      SKIP_DBT=true ;;
    esac
done

echo "=== ecom-data-pipeline — Full Run ==="

# 1. Ensure services are up
echo "[1/4] Checking services..."
docker exec postgres pg_isready -U user -d ecom_dw > /dev/null || { echo "PostgreSQL not ready"; exit 1; }
echo "  PostgreSQL OK"

# 2. Send events to Kafka
if [ "$SKIP_PRODUCER" = false ]; then
    echo "[2/4] Seeding Kafka topics (30s)..."
    PYTHONPATH=src timeout 30 python -m ecom_pipeline.ingestion.producer || true
    echo "  Kafka seeded"
else
    echo "[2/4] Skipping Kafka producer"
fi

# 3. Run batch Spark ETL
echo "[3/4] Running Spark batch ETL..."
spark-submit spark_jobs/parse_events.py \
    --input "${SPARK_INPUT:-./data/raw/}" \
    --output "${SPARK_OUTPUT:-./data/staging/}" || true
echo "  Spark ETL done"

# 4. Run dbt
if [ "$SKIP_DBT" = false ]; then
    echo "[4/4] Running dbt..."
    cd dbt
    dbt run --profiles-dir .
    dbt test --profiles-dir .
    cd ..
    echo "  dbt done"
else
    echo "[4/4] Skipping dbt"
fi

echo ""
echo "=== Pipeline complete. Check API: http://localhost:8090/api/v1/analytics/summary ==="
