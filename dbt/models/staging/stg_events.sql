with raw as (
  select * from ecom_raw.events
)
select
  event_id,
  coalesce(user_id, anonymous_id) as user_id,
  event_type,
  cast(event_ts as datetime) as event_dt,
  properties,
  session_id
from raw;
