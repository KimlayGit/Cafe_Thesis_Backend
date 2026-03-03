package com.example.backend.Controller;

import com.example.backend.Model.User;
import com.example.backend.Repository.UserRepository;
import com.example.backend.dto.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        // 1. Find user in tbl_user
        Optional<User> user = userRepository.findByUserName(loginRequest.getUsername());

        if (user.isPresent()) {
            // 2. Check password (Plain text for now to match your DB)
            if (user.get().getPassword().equals(loginRequest.getPassword())) {
                return ResponseEntity.ok(user.get()); // Returns user data to Frontend
            }
        }

        // 3. Return 401 if unauthorized
        return ResponseEntity.status(401).body("Invalid username or password");
    }
}