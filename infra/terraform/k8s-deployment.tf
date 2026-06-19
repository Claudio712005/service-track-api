resource "kubectl_manifest" "deploy" {
  depends_on = [kubectl_manifest.namespace]
  yaml_body = <<YAML
apiVersion: app/v1
kind: Deployment
metadata:
  name: nginx-deployment
  namespace: nginx
spec:
  rules:
  - http:
      paths:
      - path: /testpath
        pathType: "Prefix"
        backend:
          serviceName: test
          servicePort: 80
YAML
}