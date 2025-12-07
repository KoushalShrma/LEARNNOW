package me.learn.now.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    @GetMapping("/")
    public String home() {
        return """
               LearNnow Backend API is running! 🚀
               Frontend: http://localhost:3000
               API Docs: http://localhost:8080/swagger-ui.html
               Auth Endpoints: /api/auth/login, /api/auth/register""";
    }

    @GetMapping("/health")
    public String health() {
        return "OK - Backend is healthy! ✅";
    }
}
