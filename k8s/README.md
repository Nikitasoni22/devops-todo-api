# Kubernetes deployment

    kubectl apply -f k8s/
    kubectl get pods
    kubectl scale deployment todo-api --replicas=5

- deployment.yaml: 3 replicas, liveness and readiness probes, config from the ConfigMap
- configmap.yaml: APP_PORT
- service.yaml: NodePort service selecting pods with label app=todo-api
