with raw as (
  select * from ecom_raw.orders
)
select
  order_id,
  customer_id,
  cast(created_at as datetime) as order_dt,
  status,
  cast(total_amount as decimal(18,2)) as total_amount,
  payment_method,
  raw_metadata
from raw;
