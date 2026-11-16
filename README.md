# Todo API – DevOps CI/CD Pipeline & Kubernetes Deployment

![CI](https://github.com/Nikitasoni22/devops-todo-api/actions/workflows/ci.yml/badge.svg)

A small Spring Boot REST API used to demonstrate an end-to-end DevOps pipeline:

**Git → Maven → Jenkins → Ansible → Docker → Kubernetes**

Built for the *DevOps* course (24CYAEC59), M. S. Ramaiah Institute of Technology.

---

## Table of contents
1. [The application](#the-application)
2. [Repository layout](#repository-layout)
3. [Run locally](#run-locally)
4. [Component 1 – CI/CD (Git, Maven, Jenkins)](#component-1--cicd-git-maven-jenkins)
5. [Component 2 – Ansible and Kubernetes](#component-2--ansible-and-kubernetes)
6. [Enhancements](#enhancements)
7. [Branching workflow](#branching-workflow)
8. [Evidence](#evidence)

---

## The application

Java 17, Spring Boot 3.3.5, Maven. Todos are stored in an in-memory list, so there is no database to set up.

| Method | Path | Description |
|--------|------|-------------|
| GET | `/` | Service status (`{"service":"todo-api","status":"running"}`) |
| GET | `/todos` | List all todos |
| POST | `/todos` | Create a todo from `{"title": "..."}` (201; 400 if the title is missing) |
| GET | `/actuator/health` | Health endpoint (also `/liveness` and `/readiness`) |

The listening port is read from the `APP_PORT` environment variable and defaults to `8080`.

## Repository layout

```
.
├── src/                      Spring Boot application and JUnit tests
├── pom.xml                   Maven build (produces target/todo-api.jar)
├── Jenkinsfile               Jenkins declarative pipeline
├── .github/workflows/ci.yml  GitHub Actions workflow
├── ansible/                  Inventory, playbook and the todoapp role
├── Dockerfile                Multi-stage image build
├── k8s/                      ConfigMap, Deployment and Service manifests
└── Evidences/                Screenshots supporting the report
```

## Run locally

Requires JDK 17 and Maven.

```bash
mvn clean package
java -jar target/todo-api.jar

curl localhost:8080/
curl localhost:8080/todos
curl -X POST localhost:8080/todos \
  -H "Content-Type: application/json" \
  -d '{"title":"Test"}'
```

Run only the tests with `mvn test`.

---

## Component 1 – CI/CD (Git, Maven, Jenkins)

### Jenkins pipeline
The `Jenkinsfile` is read from this repository (*Pipeline script from SCM*, branch `*/main`).

| Stage | What it does |
|-------|--------------|
| Checkout | Checks out the source from SCM |
| Test | `mvn -B clean test`; results published as JUnit XML |
| Build | `mvn -B package -DskipTests` (tests already ran in the previous stage) |
| Version Artifact | Copies the JAR to `todo-api-<build number>-<short SHA>.jar` |
| Archive | Archives the versioned JAR with a fingerprint |

A `post` section prints whether the build succeeded.

**Trigger:** *Poll SCM* with schedule `* * * * *` (every minute). Polling is used instead of a
webhook because a local Jenkins server is not reachable from GitHub.

**Jenkins setup:** the pipeline expects the tools `Maven3` and `JDK17` to be configured under
*Manage Jenkins → Tools*, and the job to use this repository's URL with script path `Jenkinsfile`.

### GitHub Actions
`.github/workflows/ci.yml` builds and tests on every push (and on pull requests to `main`) using
JDK 17, then uploads the JAR as a build artifact named after the commit SHA.

---

## Component 2 – Ansible and Kubernetes

### Ansible
The `todoapp` role installs Java 17, creates an unprivileged service user, copies the JAR, installs a
systemd unit and starts the service. Handlers restart the service only when the JAR or the unit file
changes, so re-running the playbook on an unchanged host makes zero changes.

```bash
mvn clean package                         # build the JAR first
cd ansible
ansible-playbook -i inventory playbook.yml
sudo systemctl status todo-api
```

Override the port if needed: `ansible-playbook -i inventory playbook.yml -e app_port=8090`.
Add `--ask-become-pass` if `sudo` needs a password.

### Docker
The multi-stage `Dockerfile` compiles with Maven in a build stage and ships only the JAR on a slim
JRE-only image.

```bash
docker build -t todo-api:1.0 .
docker run -d --name todo-api-container -p 8080:8080 todo-api:1.0
curl localhost:8080/todos
```

### Kubernetes
The manifests in `k8s/` create a ConfigMap (`APP_PORT`), a Deployment with 3 replicas and
liveness/readiness probes, and a NodePort Service (port 80 → 8080, node port 30080).
Pods and the Service are connected by the label `app: todo-api`.

**Docker Desktop (built-in Kubernetes):** the locally built image is used directly.

```bash
docker build -t todo-api:1.0 .
kubectl apply -f k8s/
kubectl get deployments,pods,services

# NodePort may not be reachable from WSL, so forward the Service instead
kubectl port-forward service/todo-api-service 8092:80
curl localhost:8092/todos
```

**Minikube:**

```bash
minikube start
eval $(minikube docker-env) && docker build -t todo-api:1.0 .
kubectl apply -f k8s/
minikube service todo-api-service --url
```

### Scaling
```bash
kubectl scale deployment todo-api --replicas=5
kubectl rollout status deployment/todo-api --timeout=180s
kubectl get pods
kubectl scale deployment todo-api --replicas=3
```

---

## Enhancements

| Area | Enhancement |
|------|-------------|
| CI | Automated JUnit tests run before packaging |
| CI | Artifact versioning with build number + commit SHA |
| CI | GitHub Actions workflow on every push |
| Deployment | `APP_PORT` supplied from a Kubernetes ConfigMap |
| Deployment | Liveness and readiness probes on the Actuator health endpoints |
| Deployment | Multi-stage Dockerfile |
| Application | Health endpoint via Spring Boot Actuator |

## Branching workflow

Work is done on feature branches and merged into `main` through pull requests:

| Branch | Content | PR |
|--------|---------|----|
| `feature/todo-api` | Application and unit tests | #1 |
| `feature/jenkins-ci` | Jenkinsfile and GitHub Actions workflow | #2 |
| `feature/ansible` | Ansible inventory, playbook and role | #3 |
| `feature/docker-k8s` | Dockerfile, Kubernetes manifests, ConfigMap | #4 |

## Evidence

Screenshots of the Jenkins pipeline, GitHub Actions, Ansible runs, the Docker build and the
Kubernetes deployment and scaling are in the [`Evidences/`](Evidences/README.md) folder.

## API endpoints
- GET / : service status
- GET /todos : list todos
- POST /todos : add a todo (JSON body with a title)
- GET /actuator/health : health check
