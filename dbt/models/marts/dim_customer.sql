-- models/marts/dim_customer.sql
with src as (
  select distinct customer_id, first_name, last_name, email, country, created_at
  from stg_orders
  where customer_id is not null
)

select
  customer_id,
  md5(lower(email)) as email_hash,
  first_name,
  last_name,
  country,
  cast(min(created_at) as timestamp) as created_at
from src
group by 1,2,3,4,5
