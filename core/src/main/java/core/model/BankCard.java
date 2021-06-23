package core.model;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "BankCard")
public class BankCard {

    @Id
    @Column(name = "cardId", nullable = false, unique = true)
    private String cardId;

    @Column(name = "cardNumber", nullable = false)
    private String cardNumber;

    @Column(name = "cvc", nullable = false)
    private String cvc;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "currency", nullable = false)
    private String currency;

    @ManyToMany(mappedBy = "bankCards")
    Set<User> users;

    public BankCard() {
        amount = 0L;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCvc() {
        return cvc;
    }

    public void setCvc(String cvc) {
        this.cvc = cvc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
