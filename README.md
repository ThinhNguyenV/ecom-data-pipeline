# ecom-data-pipeline

> **E-Commerce Data Pipeline** — End-to-end data engineering project (Middle Level)
> Stack: Kafka · PySpark · Elasticsearch · dbt · PostgreSQL · Airflow · Spring Boot 3

[![CI](https://github.com/your-org/ecom-data-pipeline/actions/workflows/ci.yml/badge.svg)](https://github.com/your-org/ecom-data-pipeline/actions/workflows/ci.yml)
[![Python 3.10+](https://img.shields.io/badge/python-3.10%2B-blue.svg)](https://www.python.org)
[![Java 17](https://img.shields.io/badge/java-17-orange.svg)](https://adoptium.net)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

---

## Architecture

See [docs/architecture.md](docs/architecture.md) for the full system diagram and design decisions.

```
Kafka → PySpark Streaming → Elasticsearch (vectors)
                ↓
       Airflow (hourly DAG)
                ↓
    Spark batch ETL → dbt → PostgreSQL (marts)
                                  ↓
                     Spring Boot REST API (port 8090)
                                  ↓
                        Swagger UI · Actuator
```

---

## Tech Stack

| Layer | Technology | Version |
|---|---|---|
| Message Broker | Apache Kafka (Confluent) | 7.0.1 |
| Stream Processing | PySpark Structured Streaming | 3.5.0 |
| Embeddings | SentenceTransformers | all-MiniLM-L6-v2 |
| Search | Elasticsearch | 8.10.2 |
| Orchestration | Apache Airflow | 2.7.0 |
| Transformation | dbt-postgres | 1.7.0 |
| Data Warehouse | PostgreSQL | 13 |
| REST API | Spring Boot | 3.2.5 / Java 17 |
| API Docs | SpringDoc OpenAPI | Swagger UI |
| Infrastructure | Terraform (AWS) | provider v5 |
| Containerisation | Docker Compose | v3.8 |
| CI/CD | GitHub Actions | — |
| Data Quality | Great Expectations | 0.18.0 |

---

## Quick Start

### Prerequisites
- Docker & Docker Compose
- Python 3.10+
- Java 17+ (for local backend dev)
- Maven 3.9+ (optional — Docker handles builds)

### 1. First-time setup
```bash
git clone https://github.com/your-org/ecom-data-pipeline
cd ecom-data-pipeline

# Install Python package + dev tools, start core services, setup dbt
bash scripts/setup_dev.sh
```

### 2. Start all services
```bash
make docker-up
```

| Service | URL |
|---|---|
| **Spring Boot API** | http://localhost:8090 |
| **Swagger UI** | http://localhost:8090/swagger-ui.html |
| **Actuator Health** | http://localhost:8090/actuator/health |
| Airflow | http://localhost:8081 |
| Kibana | http://localhost:5601 |
| Spark UI | http://localhost:8080 |
| PostgreSQL | localhost:5432 |
| Elasticsearch | http://localhost:9200 |

> **Note:** PostgreSQL is auto-initialised by `deploy/sql/init.sql` on first start.

### 3. Run the pipeline
```bash
make kafka-producer    # Start producing events (Ctrl+C to stop)
make dbt-run           # Transform raw → staging → marts
make dbt-test          # Run data quality checks
```

### 4. Explore the API
```
GET  /api/v1/products?category=Footwear&page=0&size=10
GET  /api/v1/orders?status=completed
GET  /api/v1/analytics/summary?trendMonths=6&topProducts=5
GET  /api/v1/search?q=giày chạy bộ bền
```

---

## Project Structure

```
ecom-data-pipeline/
│
├── .github/
│   └── workflows/
│       ├── ci.yml              Python lint + tests + dbt + Java tests + Docker build
│       └── release.yml         Tag-triggered Docker image build + GitHub release
│
├── .gitignore                  Python · Java · Docker · Spark · dbt · Terraform
├── .dockerignore
├── .pre-commit-config.yaml     Black · isort · Flake8 · sqlfluff · terraform fmt
├── Makefile                    Developer workflow: install lint test docker dbt backend
├── pyproject.toml              PEP 517 packaging, pytest config, black/isort/mypy config
│
├── backend/                    ── Spring Boot 3.2 REST API ──
│   ├── Dockerfile              Multi-stage build (eclipse-temurin:17)
│   ├── pom.xml                 Web · JPA · Elasticsearch · Actuator · SpringDoc · Lombok
│   └── src/
│       ├── main/java/com/ecom/pipeline/
│       │   ├── config/         OpenApiConfig · ElasticsearchConfig
│       │   ├── controller/     ProductController · OrderController · CustomerController
│       │   │                   AnalyticsController · SearchController
│       │   ├── dto/            ApiResponse<T> · ProductDto · OrderDto · CustomerDto
│       │   │                   AnalyticsSummaryDto · SearchResultDto
│       │   ├── entity/         DimProduct · FactOrder · DimCustomer (JPA ↔ ecom_marts)
│       │   ├── exception/      ResourceNotFoundException · GlobalExceptionHandler
│       │   ├── mapper/         ProductMapper · OrderMapper · CustomerMapper  ← clean arch
│       │   ├── repository/     JPA repositories + native SQL analytics queries
│       │   └── service/        ProductService · OrderService · CustomerService
│       │                       AnalyticsService · SearchService
│       └── resources/
│           ├── application.yml       Base config (env-var driven)
│           ├── application-dev.yml   SQL logging · all actuator endpoints · debug logs
│           ├── application-prod.yml  Restricted actuator · larger pool · INFO logs
│           └── logback-spring.xml    Coloured console (dev) · JSON Logstash (prod)
│
├── dags/
│   └── ecom_pipeline.py        Airflow DAG: airbyte_sync >> spark >> dbt_run >> dbt_test
│
├── dbt/
│   ├── models/
│   │   ├── staging/            stg_orders · stg_order_items · stg_events · stg_customers
│   │   └── marts/              dim_product · dim_customer · fact_orders
│   ├── seeds/                  customers.csv  (loaded via dbt seed)
│   ├── snapshots/              snap_products.sql  (SCD Type 2)
│   ├── tests/                  assert_no_negative_prices · assert_orders_have_valid_customers
│   ├── macros/
│   ├── dbt_project.yml         model/seed/snapshot paths + schema config
│   └── profiles.yml            PostgreSQL connection (env-var driven)
│
├── deploy/
│   ├── docker/
│   │   ├── docker-compose.yml          Full stack + healthchecks
│   │   └── docker-compose.override.yml Dev: volume mounts + remote debug port (5005)
│   └── sql/
│       └── init.sql            ecom_raw schema + seed data (auto-mounted at startup)
│
├── docs/
│   ├── architecture.md         System diagram · data models · design decisions
│   └── runbook.md              Startup · common ops · troubleshooting · monitoring
│
├── infra/terraform/
│   ├── main.tf                 AWS S3 + IAM (provider v5 compatible)
│   ├── variables.tf
│   ├── outputs.tf
│   └── environments/
│       ├── dev.tfvars
│       └── prod.tfvars
│
├── scripts/
│   ├── setup_dev.sh            First-time dev environment setup
│   └── run_pipeline.sh         Full end-to-end pipeline run
│
├── spark_jobs/
│   └── parse_events.py         Batch ETL: JSON → partitioned Parquet
│
├── src/                        ── Python package (src layout, PEP 517) ──
│   └── ecom_pipeline/
│       ├── __init__.py         version = "1.0.0"
│       ├── api/
│       │   └── search_service.py   EcomSearchService (Elasticsearch kNN)
│       ├── common/
│       │   ├── config_loader.py    YAML config + env-var override
│       │   └── logging_config.py   Centralised logging (JSON / human-readable)
│       ├── ingestion/
│       │   └── producer.py         EcomDataProducer (Kafka)
│       └── processing/
│           └── streaming_job.py    EcomStreamingJob (foreachBatch pattern)
│
├── tests/
│   ├── conftest.py             Shared pytest fixtures (env, sample_config, sample_product)
│   ├── unit/
│   │   ├── test_producer.py    10 unit tests for EcomDataProducer
│   │   └── test_search_service.py  8 unit tests for EcomSearchService
│   └── integration/            Placeholder (requires real services)
│
├── config/
│   └── settings.yaml           Python services config (Kafka · Spark · ES · PostgreSQL)
│
└── great_expectations/
    └── expectations/           Data quality expectation suites
```

---

## API Reference

Full interactive docs → **http://localhost:8090/swagger-ui.html**

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/products` | List (paged + filter by category/keyword) |
| `GET` | `/api/v1/products/{id}` | Product detail |
| `POST` | `/api/v1/products` | Create product |
| `PUT` | `/api/v1/products/{id}` | Update product |
| `DELETE` | `/api/v1/products/{id}` | Delete product |
| `GET` | `/api/v1/products/categories` | All categories |
| `GET` | `/api/v1/orders` | Orders (paged + filter by status) |
| `GET` | `/api/v1/orders/{id}` | Order detail |
| `GET` | `/api/v1/orders/customer/{id}` | Orders by customer |
| `GET` | `/api/v1/customers` | Customers (paged + filter by country) |
| `GET` | `/api/v1/customers/{id}` | Customer detail |
| `GET` | `/api/v1/analytics/summary` | Dashboard KPIs + trend + top products |
| `GET` | `/api/v1/search?q=` | Full-text product search (Elasticsearch) |

---

## Development Commands

```bash
make help            # Show all commands
make install-dev     # Install Python dev dependencies
make lint            # flake8 + mypy
make format          # black + isort
make test            # All Python unit tests
make test-cov        # Tests + HTML coverage report
make docker-up       # Start all services
make dbt-run         # Transform data
make backend-run     # Spring Boot local dev (dev profile)
make clean           # Remove build artifacts
```

---

## Contributing

1. `bash scripts/setup_dev.sh` — first-time setup (installs pre-commit hooks)
2. Create a feature branch: `git checkout -b feat/your-feature`
3. Pre-commit runs automatically on commit (black, isort, flake8, sqlfluff)
4. PR to `develop` branch → CI runs all 5 jobs automatically

