package core.service;

import core.model.User;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> findAll(Sort sort);
    Optional<User> getUser(Long id);

    Optional<User> validateToken(String token);
    boolean hasBankCard(User user, String cardId);
    boolean hasSufficientFunds(User user, String cardId, Long amount);
}
