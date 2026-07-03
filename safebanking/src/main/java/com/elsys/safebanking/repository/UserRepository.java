package com.elsys.safebanking.repository;

import com.elsys.safebanking.model.User;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory user store.
 *
 * Replace this with a real JPA/R2DBC repository (and annotate the entity)
 * once a database is wired up — the service layer won't need to change.
 */
@Repository
public class UserRepository {

    // username (e-mail) → User
    private final Map<String, User> store = new ConcurrentHashMap<>();

    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(store.get(username));
    }

    public boolean existsByUsername(String username) {
        return store.containsKey(username);
    }

    public void save(User user) {
        store.put(user.getUsername(), user);
    }
}
