package com.elsys.safebanking.service;

import com.elsys.safebanking.model.AppUser;
import com.elsys.safebanking.model.UserRole;
import com.elsys.safebanking.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Component
public class AdminAccountSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminAccountSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;
    private final String adminFirstName;
    private final String adminLastName;

    public AdminAccountSeeder(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.email:}") String adminEmail,
            @Value("${app.admin.password:}") String adminPassword,
            @Value("${app.admin.first-name:Admin}") String adminFirstName,
            @Value("${app.admin.last-name:User}") String adminLastName
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.adminFirstName = adminFirstName;
        this.adminLastName = adminLastName;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!StringUtils.hasText(adminEmail)) {
            return;
        }

        String normalizedEmail = UserService.normalizeEmail(adminEmail);
        userRepository.findByEmailIgnoreCase(normalizedEmail).ifPresentOrElse(
                user -> {
                    if (user.getRole() != UserRole.ADMIN) {
                        user.updateRole(UserRole.ADMIN);
                        log.info("Promoted existing user {} to ADMIN", normalizedEmail);
                    }
                },
                () -> createAdmin(normalizedEmail)
        );
    }

    private void createAdmin(String normalizedEmail) {
        if (!StringUtils.hasText(adminPassword) || adminPassword.length() < 8) {
            log.warn("Skipping admin seed for {} because app.admin.password is missing or shorter than 8 characters", normalizedEmail);
            return;
        }

        userRepository.save(new AppUser(
                normalizedEmail,
                passwordEncoder.encode(adminPassword),
                StringUtils.hasText(adminFirstName) ? adminFirstName.trim() : "Admin",
                StringUtils.hasText(adminLastName) ? adminLastName.trim() : "User",
                UserRole.ADMIN
        ));
        log.info("Seeded admin account {}", normalizedEmail);
    }
}
