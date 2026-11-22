package ru.mirea.project.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.project.dto.UserBulkStatusRequest;
import ru.mirea.project.models.User;
import ru.mirea.project.repositories.UserRepository;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
public class UserBulkService {

    private final UserRepository userRepository;

    public UserBulkService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public int changeStatus(UserBulkStatusRequest request) {

        List<Long> ids = request.userIds();

        Set<Long> uniqueIds = new LinkedHashSet<>(ids);

        if (uniqueIds.size() != ids.size()) {
            throw new IllegalArgumentException(
                    "Список содержит повторяющиеся ID пользователей."
            );
        }

        List<User> users = userRepository.findAllById(uniqueIds);

        Set<Long> foundIds = new HashSet<>();

        for (User user : users) {
            foundIds.add(user.getId());
        }

        List<Long> missingIds = uniqueIds.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();

        if (!missingIds.isEmpty()) {
            throw new NoSuchElementException(
                    "Пользователи не найдены: " + missingIds
            );
        }

        for (User user : users) {
            user.setStatus(request.status());
        }

        userRepository.saveAll(users);

        return users.size();
    }
}
