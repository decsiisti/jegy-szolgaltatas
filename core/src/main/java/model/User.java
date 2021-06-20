package model;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "Users")
public class User {
    @Id
    @Column(name = "userId")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false)
    private String email;

    @OneToMany(mappedBy = "user")
    private Set<UserDevice> devices;

    @OneToMany(mappedBy = "user")
    private Set<UserToken> tokens;

    @ManyToMany
    @JoinTable(
            name = "UserBankCards",
            joinColumns = @JoinColumn(name = "userId"),
            inverseJoinColumns = @JoinColumn(name = "cardId"))
    private Set<BankCard> bankCards;

    public User() {
        id = 0L;
        name = "";
        email = "";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<UserDevice> getDevices() {
        return devices;
    }

    public Set<UserToken> getTokens() {
        return tokens;
    }

    public Set<BankCard> getBankCards() {
        return bankCards;
    }
}
