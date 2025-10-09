-- stg_order_items.sql (phiên bản chạy trực tiếp MySQL)
with raw as (
  select * from ecom_raw.order_items
)
select
  order_item_id,
  order_id,
  product_id,
  cast(quantity as signed) as quantity,
  cast(unit_price as decimal(18,2)) as unit_price
from raw;
