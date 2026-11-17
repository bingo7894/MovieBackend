package com.nerflix.clone.config;

import com.nerflix.clone.dao.UserRepository;
import com.nerflix.clone.entity.User;
import com.nerflix.clone.enums.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.countByRole(Role.ADMIN) == 0) {
            User admin = new User();
            admin.setEmail("admin@lms.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("LMS Administrator");
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            admin.setEmailVerified(true);
            userRepository.save(admin);
            log.info("Seeded default admin account: admin@lms.com / admin123");
        }

        if (!userRepository.existsByEmail("student@lms.com")) {
            User student = new User();
            student.setEmail("student@lms.com");
            student.setPassword(passwordEncoder.encode("student123"));
            student.setFullName("Demo Student");
            student.setRole(Role.USER);
            student.setActive(true);
            student.setEmailVerified(true);
            userRepository.save(student);
            log.info("Seeded default student account: student@lms.com / student123");
        }
    }
}
