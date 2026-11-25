package ru.mirea.project.account.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import ru.mirea.project.account.enums.AccountRole;
import ru.mirea.project.account.models.UserAccount;
import ru.mirea.project.account.repositories.UserAccountRepository;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Configuration
public class AdminBootstrap {

    @Bean
    public ApplicationRunner createAdmin(
            UserAccountRepository repository,
            PasswordEncoder passwordEncoder,

            @Value("${BOOTSTRAP_ADMIN_EMAIL:}")
            String email,

            @Value("${BOOTSTRAP_ADMIN_PASSWORD:}")
            String password
    ) {
        return args -> {
            System.out.println("AdminBootstrap запущен");
            System.out.println("Email задан: " + !email.isBlank());
            System.out.println("Пароль задан: " + !password.isBlank());
            System.out.println("Количество аккаунтов: " + repository.count());
            // Создаём администратора только для пустой таблицы
            if (repository.count() > 0) {
                return;
            }

            String login = email.trim()
                    .toLowerCase(Locale.ROOT);

            // Bootstrap не настроен
            if (login.isEmpty() && password.isEmpty()) {
                return;
            }

            if (login.isEmpty()) {
                throw new IllegalStateException(
                        "Не указан email администратора."
                );
            }

            if (password.isBlank() ||
                    password.length() < 4 ||
                    password.getBytes(StandardCharsets.UTF_8).length > 72) {
                throw new IllegalStateException(
                        "Некорректный пароль администратора."
                );
            }

            UserAccount admin = new UserAccount();

            admin.setFirstName("System");
            admin.setLastName("Administrator");
            admin.setEmail(login);

            admin.setPasswordHash(
                    passwordEncoder.encode(password)
            );

            admin.setRole(AccountRole.ADMIN);
            admin.setActive(true);
            admin.setMustChangePassword(true);

            repository.save(admin);
        };
    }
}
