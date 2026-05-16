from airflow import DAG
from airflow.operators.bash import BashOperator
from datetime import datetime, timedelta

default_args = {
    'owner': 'data-eng',
    'depends_on_past': False,
    'retries': 1,
    'retry_delay': timedelta(minutes=5),
    'email_on_failure': False,
}

with DAG(
    dag_id='ecom_pipeline',
    default_args=default_args,
    start_date=datetime(2025, 1, 1),
    schedule='@hourly',          # Fix: schedule_interval is deprecated in Airflow 2.7+
    catchup=False,
    max_active_runs=1,
    tags=['ecom', 'pipeline'],
) as dag:

    airbyte_sync = BashOperator(
        task_id='airbyte_sync',
        bash_command=(
            'curl -s -X POST http://{{ var.value.get("airbyte_host", "airbyte:8000") }}/api/v1/connections/sync '
            '-H "Content-Type: application/json" '
            '-d \'{"connectionId":"{{ var.value.airbyte_connection_id }}"}\' || true'
        )
    )

    run_spark_staging = BashOperator(
        task_id='run_spark_staging',
        bash_command=(
            'spark-submit --master {{ var.value.get("spark_master", "local[*]") }} '
            '/opt/spark_jobs/parse_events.py '
            '--input {{ var.value.get("s3_raw_path", "s3://ecom-raw/") }} '
            '--output {{ var.value.get("s3_staging_path", "s3://ecom-staging/") }}'
        )
    )

    dbt_run = BashOperator(
        task_id='dbt_run',
        bash_command='cd /opt/dbt && dbt deps && dbt seed --profiles-dir . && dbt run --profiles-dir .'
    )

    dbt_test = BashOperator(
        task_id='dbt_test',
        bash_command='cd /opt/dbt && dbt test --profiles-dir .'
    )

    # Fix: define task execution order
    airbyte_sync >> run_spark_staging >> dbt_run >> dbt_test