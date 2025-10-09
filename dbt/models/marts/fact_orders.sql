with orders as (
  select * from ecom_staging.stg_orders
),
items as (
  select * from ecom_staging.stg_order_items
)
select
  o.order_id,
  o.customer_id,
  o.order_dt,
  o.status,
  o.payment_method,
  o.total_amount,
  coalesce(sum(i.quantity * i.unit_price), 0) as computed_total
from orders o
left join items i on o.order_id = i.order_id
group by
  o.order_id, o.customer_id, o.order_dt, o.status, o.payment_method, o.total_amount;
