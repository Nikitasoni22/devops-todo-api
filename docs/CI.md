# CI pipeline

- Jenkins job: todo-api-pipeline (Pipeline script from SCM, branch main, Poll SCM every minute)
- Stages: Checkout, Test, Build, Version Artifact, Archive
- Artifacts are named todo-api-<build number>-<short commit hash>.jar
- GitHub Actions (.github/workflows/ci.yml) builds and tests on every push
