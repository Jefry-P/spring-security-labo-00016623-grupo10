package com.server.app.components;

import com.server.app.entities.Role;
import com.server.app.entities.User;
import com.server.app.repositories.RoleRepository;
import com.server.app.repositories.UserRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        Role adminRole = roleRepository.findByName("ADMIN").orElseGet(() -> {
            Role r = new Role();
            r.setName("ADMIN");
            r.setActive(true);
            return roleRepository.save(r);
        });
        if (adminRole.getActive() == null || !adminRole.getActive()) {
            adminRole.setActive(true);
            adminRole = roleRepository.save(adminRole);
        }

        Role userRole = roleRepository.findByName("USER").orElseGet(() -> {
            Role r = new Role();
            r.setName("USER");
            r.setActive(true);
            return roleRepository.save(r);
        });
        if (userRole.getActive() == null || !userRole.getActive()) {
            userRole.setActive(true);
            roleRepository.save(userRole);
        }

        if (userRepository.findUserByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setName("Admin");
            admin.setSurname("System");
            admin.setEmail("admin@server.com");
            admin.setPassword(passwordEncoder.encode("00016623"));
            admin.setRole(adminRole);
            admin.setBlocked(false);
            userRepository.save(admin);
        } else {
            User admin = userRepository.findUserByUsername("admin").get();
            admin.setRole(adminRole);
            admin.setBlocked(false);
            admin.setPassword(passwordEncoder.encode("00016623"));
            userRepository.save(admin);
        }
    }
}
