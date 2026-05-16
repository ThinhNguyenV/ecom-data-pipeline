# ============================================================
# Makefile — ecom-data-pipeline
# Common developer commands. Run: make <target>
# Requires: Python 3.10+, Docker, Maven 3.9+
# ============================================================

.PHONY: help install install-dev lint format test test-unit test-cov \
        docker-up docker-down docker-logs \
        dbt-run dbt-test dbt-docs \
        kafka-producer spark-streaming \
        backend-run backend-test backend-build \
        clean

# ── Default: show help ────────────────────────────────────
help:
	@echo ""
	@echo "  ecom-data-pipeline — Developer Commands"
	@echo "  ========================================"
	@echo ""
	@echo "  Setup"
	@echo "    make install          Install Python runtime deps"
	@echo "    make install-dev      Install all deps incl. dev tools"
	@echo "    make pre-commit       Install pre-commit hooks"
	@echo ""
	@echo "  Code Quality"
	@echo "    make lint             Run flake8 + mypy"
	@echo "    make format           Run black + isort"
	@echo ""
	@echo "  Testing"
	@echo "    make test             Run all Python unit tests"
	@echo "    make test-unit        Run unit tests only"
	@echo "    make test-cov         Run tests with coverage report"
	@echo ""
	@echo "  Infrastructure"
	@echo "    make docker-up        Start all services (Docker Compose)"
	@echo "    make docker-down      Stop all services"
	@echo "    make docker-logs      Tail all container logs"
	@echo ""
	@echo "  Pipeline"
	@echo "    make dbt-run          Run dbt models"
	@echo "    make dbt-test         Run dbt tests"
	@echo "    make dbt-docs         Serve dbt docs locally"
	@echo "    make kafka-producer   Start Kafka event producer"
	@echo "    make spark-streaming  Start PySpark streaming job"
	@echo ""
	@echo "  Backend (Spring Boot)"
	@echo "    make backend-run      Run Spring Boot locally (dev profile)"
	@echo "    make backend-test     Run Java unit tests"
	@echo "    make backend-build    Build backend JAR"
	@echo ""
	@echo "  Cleanup"
	@echo "    make clean            Remove build artefacts"
	@echo ""

# ── Setup ─────────────────────────────────────────────────
install:
	pip install -e ".[quality]"

install-dev:
	pip install -e ".[dev,quality]"

pre-commit:
	pre-commit install
	pre-commit install --hook-type commit-msg

# ── Code Quality ──────────────────────────────────────────
lint:
	flake8 src/ spark_jobs/ --max-line-length=120
	mypy src/ecom_pipeline --ignore-missing-imports

format:
	black src/ spark_jobs/ tests/ --line-length=120
	isort src/ spark_jobs/ tests/ --profile=black

# ── Testing ───────────────────────────────────────────────
test:
	pytest tests/ -v

test-unit:
	pytest tests/unit/ -v

test-cov:
	pytest tests/ --cov=ecom_pipeline --cov-report=html --cov-report=term-missing

# ── Infrastructure ────────────────────────────────────────
docker-up:
	cd deploy/docker && docker compose up -d
	@echo "Services starting... check http://localhost:8090/swagger-ui.html"

docker-down:
	cd deploy/docker && docker compose down

docker-logs:
	cd deploy/docker && docker compose logs -f

docker-restart:
	cd deploy/docker && docker compose down && docker compose up -d

# ── Pipeline ──────────────────────────────────────────────
dbt-run:
	cd dbt && dbt deps && dbt run --profiles-dir .

dbt-test:
	cd dbt && dbt test --profiles-dir .

dbt-docs:
	cd dbt && dbt docs generate --profiles-dir . && dbt docs serve

kafka-producer:
	PYTHONPATH=src python -m ecom_pipeline.ingestion.producer

spark-streaming:
	PYTHONPATH=src python -m ecom_pipeline.processing.streaming_job

# ── Backend (Spring Boot) ─────────────────────────────────
backend-run:
	cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=dev

backend-test:
	cd backend && mvn test

backend-build:
	cd backend && mvn package -DskipTests

backend-docker:
	cd backend && docker build -t ecom-pipeline-backend:latest .

# ── Cleanup ───────────────────────────────────────────────
clean:
	find . -type d -name "__pycache__" -exec rm -rf {} + 2>/dev/null; true
	find . -name "*.pyc" -delete 2>/dev/null; true
	find . -name ".coverage" -delete 2>/dev/null; true
	rm -rf htmlcov/ .pytest_cache/ dist/ build/ 2>/dev/null; true
	cd backend && mvn clean -q 2>/dev/null; true
	cd dbt && rm -rf target/ dbt_packages/ 2>/dev/null; true
	@echo "Cleaned."
