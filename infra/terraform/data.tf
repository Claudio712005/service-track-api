data "aws_iam_user" "principal_user" {
  user_name = "service-user.tf"
}

data "aws_eks_cluster" "cluster" {
  value = aws_eks_cluster.cluster.name
}

data "aws_eks_cluster_auth" "auth" {
  name = data.aws_eks_cluster.cluster.name
}