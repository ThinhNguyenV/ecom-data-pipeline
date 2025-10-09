-- models/marts/dim_product.sql
with src as (
  select distinct product_id, product_name, sku, category, price, updated_at
  from raw.products
)

select
  product_id,
  sku,
  name,
  category,
  price
from (
  select
    product_id,
    sku,
    product_name as name,
    category,
    cast(price as numeric) as price,
    row_number() over (partition by product_id order by updated_at desc) as rn
  from src
) t
where rn = 1
