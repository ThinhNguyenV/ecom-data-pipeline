-- dbt/tests/assert_no_negative_prices.sql
-- Custom singular test: no product should have a negative price
-- dbt will fail if this query returns any rows

select
    product_id,
    price
from {{ ref('dim_product') }}
where price < 0
