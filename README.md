# Todo API – DevOps CI/CD Pipeline & Kubernetes Deployment

A small Spring Boot REST API (`GET /`, `GET /todos`, `POST /todos`) used to demonstrate
a full pipeline: **Git → Maven → Jenkins → Ansible → Docker → Kubernetes**.

## Run locally
```bash
mvn clean package
java -jar target/todo-api.jar
curl localhost:8080/todos
curl -X POST localhost:8080/todos -H "Content-Type: application/json" -d '{"title":"Test"}'
```

## Component 1 – CI
- `Jenkinsfile`: SCM polling, test stage, build, versioned artifact (build number + commit SHA), archive
- `.github/workflows/ci.yml`: GitHub Actions build + test on every push

## Component 2 – Deployment
```bash
# Ansible
cd ansible && ansible-playbook -i inventory playbook.yml

# Docker
docker build -t todo-api:1.0 .
docker run -p 8080:8080 todo-api:1.0

# Kubernetes (minikube)
minikube start
eval $(minikube docker-env) && docker build -t todo-api:1.0 .
kubectl apply -f k8s/
kubectl get pods
kubectl scale deployment todo-api --replicas=5
minikube service todo-api-service --url
```

Enhancements: tests in CI, commit-SHA versioning, GitHub Actions, ConfigMap, liveness/readiness probes, multi-stage Dockerfile, health endpoint.
