# Architecture — ecom-data-pipeline

## System Design

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                              DATA SOURCES                                     │
│   E-commerce events  ·  Orders  ·  Products  (simulated by producer.py)      │
└───────────────────────────────────┬─────────────────────────────────────────-┘
                                    │
                          Kafka Topics (port 9093)
                    ┌───────────────┴────────────────┐
                    ▼                                ▼
              [products]                     [user_behavior]
                    │                                │
                    └────────────┬───────────────────┘
                                 │
                    ┌────────────▼────────────────┐
                    │  PySpark Structured Streaming│
                    │  streaming_job.py            │
                    │  • foreachBatch pattern       │
                    │  • SentenceTransformers       │
                    │    (384-dim embeddings)       │
                    └───────┬────────────┬──────────┘
                            │            │
                    Parquet Lake     Elasticsearch
                  /data/raw/       index: products
                            │
                    ┌───────▼────────────────────┐
                    │  Apache Airflow (hourly)    │
                    │  airbyte_sync               │
                    │    → spark_staging          │
                    │    → dbt_run                │
                    │    → dbt_test               │
                    └───────┬────────────────────-┘
                            │
                    ┌───────▼─────────────────────────┐
                    │         dbt (PostgreSQL)          │
                    │                                   │
                    │  ecom_raw.*     (source tables)   │
                    │       ↓ staging models            │
                    │  ecom_staging.* (cleaned)         │
                    │       ↓ mart models               │
                    │  ecom_marts.*   (analytics-ready) │
                    └───────┬─────────────────────────-┘
                            │
                ┌───────────┴──────────────┐
                │                          │
        PostgreSQL                   Elasticsearch
        (ecom_marts.*)              (products index)
                │                          │
                └───────────┬──────────────┘
                            │
                ┌───────────▼──────────────────────────┐
                │     Spring Boot REST API              │
                │     (port 8090)                       │
                │                                       │
                │  ProductController  /api/v1/products  │
                │  OrderController    /api/v1/orders    │
                │  CustomerController /api/v1/customers │
                │  AnalyticsController /api/v1/analytics│
                │  SearchController   /api/v1/search    │
                │                                       │
                │  Swagger UI: /swagger-ui.html         │
                │  Health:     /actuator/health         │
                └──────────────────────────────────────┘
```

## Layer Descriptions

| Layer | Responsibility | Tech |
|---|---|---|
| **Ingestion** | Produce events to Kafka | kafka-python |
| **Stream Processing** | Consume Kafka, embed, write to lake + ES | PySpark, SentenceTransformers |
| **Orchestration** | Schedule hourly batch jobs | Apache Airflow 2.7 |
| **Batch ETL** | Parse raw JSON → Parquet staging | PySpark |
| **Transformation** | Raw → Staging → Marts | dbt-postgres |
| **Storage: Operational** | Raw and staging relational data | PostgreSQL 13 |
| **Storage: Search** | Product vectors for semantic search | Elasticsearch 8.10 |
| **Serving** | REST API, analytics, search | Spring Boot 3.2 |

## Data Models

### Raw Layer (`ecom_raw`)
- `customers` — customer master data
- `products` — product catalogue
- `orders` — order headers
- `order_items` — order line items
- `events` — clickstream events

### Staging Layer (`ecom_staging`)
- `stg_customers` — cleansed customers
- `stg_orders` — typed + cleansed orders
- `stg_order_items` — typed + cleansed order items
- `stg_events` — normalised events

### Marts Layer (`ecom_marts`)
- `dim_product` — product dimension (latest, SCD1)
- `dim_customer` — customer dimension (PII hashed)
- `fact_orders` — order fact table with computed totals

## Design Decisions

1. **foreachBatch vs. UDF for embeddings**: SentenceTransformer cannot be serialised as a Spark UDF. We use `foreachBatch` to load the model once per micro-batch on the driver. Acceptable for batch sizes typical in streaming.

2. **ddl-auto=none in Spring Boot**: dbt owns the PostgreSQL schema. JPA must not alter tables — it is read-only from the data warehouse perspective.

3. **PostgreSQL over BigQuery**: self-hosted stack, no GCP dependency. Swap profiles.yml to `dbt-bigquery` if migrating to cloud.

4. **Email hashing**: `dim_customer.email_hash` stores MD5(lower(email)) — PII is never exposed via API. CustomerDto intentionally omits raw email.
