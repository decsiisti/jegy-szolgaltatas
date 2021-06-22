package core.service;

import core.model.BankCard;
import core.model.User;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import core.repository.UserRepository;

import java.util.Base64;
import java.util.List;
import java.util.Optional;


@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> findAll(Sort sort) {
        return userRepository.findAll(sort);
    }

    @Override
    public Optional<User> getUser(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> validateToken(String token) {
        String decodedToken = new String( Base64.getDecoder().decode(token) );
        String [] parts = decodedToken.split("&");
        if(parts.length != 3)
            return Optional.empty();

        return userRepository.findById(Long.valueOf(parts[1]));
    }

    @Override
    public boolean hasBankCard(User user, String cardId) {
        return user.getBankCards().stream().anyMatch(card -> cardId.equals(card.getCardId()));
    }

    @Override
    public boolean hasSufficientFunds(User user, String cardId, Long amount) {
        return user.getBankCards().stream().anyMatch(card -> cardId.equals(card.getCardId()) && card.getAmount() >= amount);
    }

    @Override
    public boolean decreaseBalance(User user, String cardId, Long amount) {
        Optional<BankCard> c = user.getBankCards().stream().filter(card -> cardId.equals(card.getCardId())).findAny();
        if(c.isEmpty()) {
            return false;
        }

        BankCard card = c.get();
        if(card.getAmount() >= amount) {
            card.setAmount(card.getAmount() - amount);
            userRepository.saveAndFlush(user);
            return true;
        }

        return false;
    }

    @Override
    public boolean increaseBalance(User user, String cardId, Long amount) {
        Optional<BankCard> c = user.getBankCards().stream().filter(card -> cardId.equals(card.getCardId())).findAny();
        if(c.isEmpty()) {
            return false;
        }

        BankCard card = c.get();
        card.setAmount(card.getAmount() + amount);
        userRepository.saveAndFlush(user);
        return true;
    }
}
