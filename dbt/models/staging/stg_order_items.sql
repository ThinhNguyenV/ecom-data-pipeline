-- stg_order_items.sql
with raw as (
  select * from {{ source('raw', 'order_items') }}
)
select
  order_item_id,
  order_id,
  product_id,
  cast(quantity as integer)          as quantity,      -- Fix: SIGNED is MySQL-only; INTEGER is PostgreSQL compatible
  cast(unit_price as numeric(18, 2)) as unit_price
from raw
where order_item_id is not null
