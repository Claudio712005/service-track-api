variable "projectName" {
  default = "service-track"
}

variable "region_default" {
  default = "us-east-1"
}

variable "cidr_vpc" {
  default = "10.0.0.0/16"
}

variable "tags" {
  default = {
    Name = "service-track-terraform"
  }
}

variable "instace_type" {
  default = "t3.medium"
}