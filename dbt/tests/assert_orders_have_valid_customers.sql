-- dbt/tests/assert_orders_have_valid_customers.sql
-- Custom singular test: every order must reference an existing customer
-- dbt will fail if orphaned orders are found

select
    o.order_id,
    o.customer_id
from {{ ref('fact_orders') }} o
left join {{ ref('dim_customer') }} c on o.customer_id = c.customer_id
where c.customer_id is null
