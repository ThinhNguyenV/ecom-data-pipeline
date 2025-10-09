output "s3_raw_bucket" {
  value = aws_s3_bucket.raw.bucket
}
output "data_pipeline_role_arn" {
  value = aws_iam_role.data_pipeline_role.arn
}
