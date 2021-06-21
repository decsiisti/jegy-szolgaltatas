package core;

import core.model.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import core.service.UserServiceImpl;

@RestController
public class UserController {

    private final UserServiceImpl userService;

    UserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @GetMapping("/validateToken")
    boolean validateToken(@RequestParam String token)
    {
        if(token.isEmpty()) {
            throw new MyException(10050L, "No user token provided with the request.");
        }

        userService.validateToken(token).orElseThrow(() -> new MyException(10051L, "The provided user token is expired or not recognizable."));

        return true;
    }

    @GetMapping("/hasBankCard")
    boolean hasBankCard(@RequestParam Long userId, @RequestParam String cardId)
    {
        User user = userService.getUser(userId).orElseThrow(() -> new MyException(10099L, "User with userId '" + userId + "' doesn't exist."));

        if(userService.hasBankCard(user, cardId)) {
            return true;
        } else {
            throw new MyException(10100L, "The Card provided in the request doesn't belong to the requested User.");
        }
    }

    @GetMapping("/hasSufficientFunds")
    boolean hasSufficientFunds(@RequestParam Long userId, @RequestParam String cardId, @RequestParam Long amount)
    {
        User user = userService.getUser(userId).orElseThrow(() -> new MyException(10099L, "User with userId '" + userId + "' doesn't exist."));

        if(!userService.hasBankCard(user, cardId)) {
            throw new MyException(10100L, "The Card provided in the request doesn't belong to the requested User.");
        } else if(!userService.hasSufficientFunds(user, cardId, amount)){
            throw new MyException(10101L, "The Card provided in the request doesn't have enough funds.");
        }

        return true;
    }
}
