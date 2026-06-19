resource "aws_vpc" "vpc_service_track" {
  cidr_block = var.cidr_vpc
  enable_dns_hostnames = true
  enable_dns_support = true

  tags = var.tags
}