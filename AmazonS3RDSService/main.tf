terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
  }

  required_version = ">= 1.5.0"
}

provider "aws" {
  region = "ap-south-1"
  access_key = ""
  secret_key = ""
}

# =========================================================
# S3 BUCKET
# =========================================================

resource "aws_s3_bucket" "app_bucket" {
  bucket = "my-java-app-bucket-2026-unique"

  tags = {
    Name        = "Java Application S3 Bucket"
    Environment = "dev"
  }
}

# Block public access
resource "aws_s3_bucket_public_access_block" "app_bucket_public_access" {

  bucket = aws_s3_bucket.app_bucket.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# Enable versioning
resource "aws_s3_bucket_versioning" "app_bucket_versioning" {

  bucket = aws_s3_bucket.app_bucket.id

  versioning_configuration {
    status = "Enabled"
  }
}


# =========================================================
# RDS MYSQL DATABASE
# =========================================================

resource "aws_db_instance" "mysql" {

  identifier = "java-app-mysql"

  engine         = "mysql"
  engine_version = "8.0"

  instance_class = "db.t3.micro"

  allocated_storage = 20
  storage_type      = "gp3"

  db_name  = "appdb"
  username = "admin"
  password = "ChangeMe123!"

  port = 3306

  # Development settings
  publicly_accessible = true

  skip_final_snapshot = true

  backup_retention_period = 0

  deletion_protection = false

  tags = {
    Name        = "Java Application MySQL"
    Environment = "dev"
  }
}


# =========================================================
# OUTPUTS
# =========================================================

output "s3_bucket_name" {
  value = aws_s3_bucket.app_bucket.bucket
}

output "s3_bucket_arn" {
  value = aws_s3_bucket.app_bucket.arn
}

output "mysql_endpoint" {
  value = aws_db_instance.mysql.endpoint
}

output "mysql_database" {
  value = aws_db_instance.mysql.db_name
}

output "mysql_username" {
  value = aws_db_instance.mysql.username
}