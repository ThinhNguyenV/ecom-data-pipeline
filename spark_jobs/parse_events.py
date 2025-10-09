#!/usr/bin/env python3
"""
Simple PySpark script to read raw JSON events from input path and write partitioned Parquet to output path.
Supports local testing and s3 (requires Spark configured with AWS creds).
"""
import argparse
from pyspark.sql import SparkSession
from pyspark.sql.functions import col, from_unixtime, to_date, expr

def main(input_path, output_path, master="local[*]"):
    spark = SparkSession.builder.appName("parse_events").getOrCreate()

    # Read JSON (support glob)
    df = spark.read.option("multiline", "false").json(input_path)

<<<<<<< HEAD
    # Basic normalisation - adapt to raw JSON schema
=======
    # Basic normalisation - adapt to your raw JSON schema
>>>>>>> a0a9c18 (first commit)
    if "timestamp" in df.columns:
        df = df.withColumn("event_dt", from_unixtime(col("timestamp")/1000))
    elif "event_ts" in df.columns:
        df = df.withColumn("event_dt", from_unixtime(col("event_ts")/1000))
    else:
        # try to cast created_at
        if "created_at" in df.columns:
            df = df.withColumn("event_dt", col("created_at").cast("timestamp"))

    df = df.withColumn("event_date", to_date(col("event_dt")))

    # write partitioned by event_date
    df.write.mode("append").partitionBy("event_date").parquet(output_path)
    spark.stop()

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Parse events and write parquet")
    parser.add_argument("--input", required=True, help="Input path (e.g. s3://bucket/raw/events/*/*.json or ./samples/)")
    parser.add_argument("--output", required=True, help="Output path (e.g. s3://bucket/staging/events/ or ./tmp/staging/)")
    args = parser.parse_args()
    main(args.input, args.output)
