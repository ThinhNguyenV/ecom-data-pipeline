# Simple Great Expectations suite runner (example)
from great_expectations.dataset import SparkDFDataset
from pyspark.sql import SparkSession

def run_expectations(parquet_path):
    spark = SparkSession.builder.appName("ge_events").getOrCreate()
    df = spark.read.parquet(parquet_path)
    dataset = SparkDFDataset(df)
    print("expect_event_id_not_null:", dataset.expect_column_values_to_not_be_null("event_id"))
    print("expect_event_type_in:", dataset.expect_column_values_to_be_in_set("event_type", ["pageview","add_to_cart","checkout","purchase"]))
    spark.stop()

if __name__ == "__main__":
    import sys
    run_expectations(sys.argv[1])
