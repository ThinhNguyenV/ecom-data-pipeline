provider "aws" {
  region = var.region
}

# S3 raw bucket
resource "aws_s3_bucket" "raw" {
  bucket = var.bucket_raw
}

# Fix: acl argument was deprecated in AWS provider v4+.
# Use separate aws_s3_bucket_acl resource instead.
resource "aws_s3_bucket_acl" "raw_acl" {
  bucket = aws_s3_bucket.raw.id
  acl    = "private"

  depends_on = [aws_s3_bucket_ownership_controls.raw_ownership]
}

resource "aws_s3_bucket_ownership_controls" "raw_ownership" {
  bucket = aws_s3_bucket.raw.id
  rule {
    object_ownership = "BucketOwnerPreferred"
  }
}

resource "aws_s3_bucket_versioning" "raw_versioning" {
  bucket = aws_s3_bucket.raw.id
  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "raw_sse" {
  bucket = aws_s3_bucket.raw.id
  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_s3_bucket_lifecycle_configuration" "raw_lifecycle" {
  bucket = aws_s3_bucket.raw.id
  rule {
    id     = "abort-incomplete-multipart"
    status = "Enabled"
    abort_incomplete_multipart_upload {
      days_after_initiation = 7
    }
  }
}

# IAM role for Airflow / Spark
data "aws_iam_policy_document" "assume_role" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com", "ec2.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "data_pipeline_role" {
  name               = "${var.prefix}-data-pipeline-role"
  assume_role_policy = data.aws_iam_policy_document.assume_role.json
}

resource "aws_iam_policy" "s3_rw_policy" {
  name = "${var.prefix}-s3-rw"
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "s3:PutObject",
          "s3:GetObject",
          "s3:ListBucket",
          "s3:DeleteObject"
        ]
        Resource = [
          aws_s3_bucket.raw.arn,
          "${aws_s3_bucket.raw.arn}/*"
        ]
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "attach_s3_rw" {
  role       = aws_iam_role.data_pipeline_role.name
  policy_arn = aws_iam_policy.s3_rw_policy.arn
}
