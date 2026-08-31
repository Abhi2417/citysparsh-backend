package com.app.citysparsh.controller;

import com.app.citysparsh.dto.AuthResponse;
import com.app.citysparsh.dto.LoginRequest;
import com.app.citysparsh.dto.RegisterRequest;
import com.app.citysparsh.dto.MessageResponse;
import com.app.citysparsh.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@RequestBody RegisterRequest request) {
        MessageResponse res= authService.register(request);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse res = authService.login(request);
            return ResponseEntity.ok(res);
        } catch (RuntimeException ex) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Invalid email or password"));
        }
    }


}
