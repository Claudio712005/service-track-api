resource "aws_s3_bucket" "bucket-backend" {
  bucket = var.projectName
  tags = var.tags
}