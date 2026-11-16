# Ansible deployment

Run from this folder on Ubuntu (WSL):

    ansible-playbook -i inventory playbook.yml --ask-become-pass

The todoapp role installs Java 17, creates a service user, copies target/todo-api.jar
and runs it as a systemd service. Variable: app_port (default 8080).
The restart handler runs only when the JAR or the unit file changes.
