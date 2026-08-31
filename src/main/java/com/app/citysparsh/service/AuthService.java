package com.app.citysparsh.service;

import com.app.citysparsh.dto.AuthResponse;
import com.app.citysparsh.dto.LoginRequest;
import com.app.citysparsh.dto.RegisterRequest;
import com.app.citysparsh.dto.MessageResponse;
import com.app.citysparsh.model.Role;
import com.app.citysparsh.model.User;
import com.app.citysparsh.repository.UserRepository;
import com.app.citysparsh.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository repo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public MessageResponse register(RegisterRequest request) {
        if(repo.findByEmail(request.getEmail()).isPresent())
            throw new RuntimeException("Email already registered!");

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.CITIZEN); // Default Role

        repo.save(user);
        return new MessageResponse("User registered successfully");
    }

    public AuthResponse login(LoginRequest request) {
        User user = repo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User Not Found!"));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new RuntimeException("Invalid Password!");

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().toString());
        String role =  user.getRole().toString();

        return new AuthResponse(
                token,
                user.getRole().name(),
                user.getFirstName() + " " + user.getLastName()
        );

    }
}
