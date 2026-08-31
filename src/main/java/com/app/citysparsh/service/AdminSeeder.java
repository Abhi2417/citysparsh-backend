package com.app.citysparsh.service;

import com.app.citysparsh.model.Role;
import com.app.citysparsh.model.User;
import com.app.citysparsh.repository.UserRepository;
import jdk.jfr.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Override
    public void run(String... args) throws Exception {

        if (userRepository.findByEmail("newadmin@citysparsh.com").isEmpty()) {

            User admin = new User();
            admin.setFirstName("New");
            admin.setLastName("Admin");
            admin.setEmail("newadmin@citysparsh.com");
            admin.setPassword(encoder.encode("admin123"));
            admin.setRole(Role.ADMIN);

            userRepository.save(admin);

            System.out.println("✔ Admin created: newadmin@citysparsh.com / admin123");
        }
    }
}
