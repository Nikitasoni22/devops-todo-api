package com.example.todo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TodoController {

    // In-memory "database" - no external DB needed for this assignment
    private final List<Todo> todos = new CopyOnWriteArrayList<>();
    private final AtomicLong counter = new AtomicLong();

    public TodoController() {
        todos.add(new Todo(counter.incrementAndGet(), "Learn Git", true));
        todos.add(new Todo(counter.incrementAndGet(), "Build CI pipeline", false));
    }

    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of("service", "todo-api", "status", "running");
    }

    @GetMapping("/todos")
    public List<Todo> list() {
        return todos;
    }

    @PostMapping("/todos")
    public ResponseEntity<Todo> add(@RequestBody Map<String, String> body) {
        String title = body.get("title");
        if (title == null || title.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        Todo todo = new Todo(counter.incrementAndGet(), title, false);
        todos.add(todo);
        return ResponseEntity.status(HttpStatus.CREATED).body(todo);
    }
}
