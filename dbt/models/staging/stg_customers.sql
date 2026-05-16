-- stg_customers.sql
-- Staging layer for raw customers table
with raw as (
  select * from {{ source('raw', 'customers') }}
)
select
  customer_id,
  first_name,
  last_name,
  lower(trim(email))                         as email,
  country,
  cast(created_at as timestamp)              as created_at
from raw
where customer_id is not null
