-- dbt/snapshots/snap_products.sql
-- SCD Type 2 snapshot for product dimension
-- Captures historical changes to product price and category
-- Run: dbt snapshot --profiles-dir .

{% snapshot snap_products %}

{{
    config(
        target_schema='ecom_snapshots',
        unique_key='product_id',
        strategy='check',
        check_cols=['product_name', 'price', 'category'],
        invalidate_hard_deletes=True,
    )
}}

select
    product_id,
    sku,
    product_name,
    category,
    price,
    updated_at
from {{ source('raw', 'products') }}

{% endsnapshot %}
