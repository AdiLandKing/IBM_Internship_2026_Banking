package com.elsys.safebanking.service;

import com.elsys.safebanking.model.UserRole;
import com.elsys.safebanking.model.Users;
import com.elsys.safebanking.repository.UserRepository;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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
    private static final DateTimeFormatter AUDIT_TIME_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

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
                        log.info(
                                "Admin seed audit action=promote-admin result=success email={} {}",
                                normalizedEmail,
                                auditContext()
                        );
                    }
                },
                () -> createAdmin(normalizedEmail)
        );
    }

    private void createAdmin(String normalizedEmail) {
        if (!StringUtils.hasText(adminPassword) || adminPassword.length() < 8) {
            log.warn(
                    "Admin seed audit action=create-admin result=skipped reason=missing-or-short-password email={} {}",
                    normalizedEmail,
                    auditContext()
            );
            return;
        }

        userRepository.save(new Users(
                normalizedEmail,
                passwordEncoder.encode(adminPassword),
                StringUtils.hasText(adminFirstName) ? adminFirstName.trim() : "Admin",
                StringUtils.hasText(adminLastName) ? adminLastName.trim() : "User",
                UserRole.ADMIN
        ));
        log.info(
                "Admin seed audit action=create-admin result=success email={} {}",
                normalizedEmail,
                auditContext()
        );
    }

    private String auditContext() {
        HostAuditInfo hostAuditInfo = resolveHostAuditInfo();
        return "timestampUtc=%s hostName=%s hostAddress=%s"
                .formatted(
                        AUDIT_TIME_FORMATTER.format(OffsetDateTime.now(ZoneOffset.UTC)),
                        hostAuditInfo.hostName(),
                        hostAuditInfo.hostAddress()
                );
    }

    private HostAuditInfo resolveHostAuditInfo() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return new HostAuditInfo(localHost.getHostName(), localHost.getHostAddress());
        } catch (UnknownHostException exception) {
            return new HostAuditInfo("unknown", "unknown");
        }
    }

    private record HostAuditInfo(String hostName, String hostAddress) {
    }
}
