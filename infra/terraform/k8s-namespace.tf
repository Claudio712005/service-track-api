resource "kubectl_manifest" "namespace" {
  depends_on = [aws_eks_cluster.cluster]
  yaml_body = <<YAML
apiVersion: app/v1
kind: Namespace
metadata:
  name: nginx
  namespace: nginx
spec:
  replicas: 3
  selector:
    matchLabels:
      app: nginx
    template:
        metadata:
            labels:
            app: nginx
        spec:
            containers:
            - name: nginx
            image: nginx:1.25
            ports:
            - containerPort: 80
YAML
}