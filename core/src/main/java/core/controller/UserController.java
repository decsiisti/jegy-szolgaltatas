package core.controller;

import core.exception.CoreErrorException;
import core.model.CoreError;
import core.model.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import core.service.UserServiceImpl;

import java.util.Optional;

@RestController
public class UserController {

    private final UserServiceImpl userService;

    UserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @GetMapping("/validateToken")
    Long validateToken(@RequestParam String token)
    {
        if(token.isEmpty()) {
            throw new CoreErrorException(new CoreError(10050L, "No user token provided with the request."));
        }

        User user = userService.validateToken(token).orElseThrow(() -> new CoreErrorException(new CoreError(10051L, "The provided user token is expired or not recognizable.")));

        return user.getId();
    }

    @GetMapping("/hasBankCard")
    boolean hasBankCard(@RequestParam Long userId, @RequestParam String cardId)
    {
        User user = userService.getUser(userId).orElseThrow(() -> new CoreErrorException(new CoreError(10099L, "User with userId '" + userId + "' doesn't exist.")));

        if(userService.hasBankCard(user, cardId)) {
            return true;
        } else {
            throw new CoreErrorException(new CoreError(10100L, "The Card provided in the request doesn't belong to the requested User."));
        }
    }

    @GetMapping("/hasSufficientFunds")
    boolean hasSufficientFunds(@RequestParam Long userId, @RequestParam String cardId, @RequestParam Long amount)
    {
        User user = userService.getUser(userId).orElseThrow(() -> new CoreErrorException(new CoreError(10099L, "User with userId '" + userId + "' doesn't exist.")));

        if(!userService.hasBankCard(user, cardId)) {
            throw new CoreErrorException(new CoreError(10100L, "The Card provided in the request doesn't belong to the requested User."));
        } else if(!userService.hasSufficientFunds(user, cardId, amount)){
            throw new CoreErrorException(new CoreError(10101L, "The Card provided in the request doesn't have enough funds."));
        }

        return true;
    }

    @PutMapping("/decreaseBalance")
    void decreaseBalance(@RequestParam Long userId, @RequestParam String cardId, @RequestParam Long amount)
    {
        User user = userService.getUser(userId).orElseThrow(() -> new CoreErrorException(new CoreError(10099L, "User with userId '" + userId + "' doesn't exist.")));

        if(!userService.hasBankCard(user, cardId)) {
            throw new CoreErrorException(new CoreError(10100L, "The Card provided in the request doesn't belong to the requested User."));
        } else if(!userService.hasSufficientFunds(user, cardId, amount)) {
            throw new CoreErrorException(new CoreError(10101L, "The Card provided in the request doesn't have enough funds."));
        } else if(!userService.decreaseBalance(user, cardId, amount)) {
            throw new CoreErrorException(new CoreError(10102L, "Could not decrease balance."));
        }
    }

    @PutMapping("/increaseBalance")
    void increaseBalance(@RequestParam Long userId, @RequestParam String cardId, @RequestParam Long amount)
    {
        User user = userService.getUser(userId).orElseThrow(() -> new CoreErrorException(new CoreError(10099L, "User with userId '" + userId + "' doesn't exist.")));

        if(!userService.hasBankCard(user, cardId)) {
            throw new CoreErrorException(new CoreError(10100L, "The Card provided in the request doesn't belong to the requested User."));
        } else if(!userService.increaseBalance(user, cardId, amount)) {
            throw new CoreErrorException(new CoreError(10103L, "Could not increase balance."));
        }
    }
}
