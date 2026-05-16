-- models/marts/dim_customer.sql
-- Fix: source changed from stg_orders (wrong) to stg_customers (correct)
with src as (
  select
    customer_id,
    first_name,
    last_name,
    email,
    country,
    created_at
  from {{ ref('stg_customers') }}
  where customer_id is not null
)

select
  customer_id,
  md5(lower(email))                 as email_hash,
  first_name,
  last_name,
  country,
  min(created_at)                   as created_at
from src
group by 1, 2, 3, 4, 5
