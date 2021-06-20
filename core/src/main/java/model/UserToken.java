package model;

import javax.persistence.*;

@Entity
@Table(name = "UserToken")
public class UserToken {
    @ManyToOne
    @JoinColumn(name="userId", nullable = false)
    private User user;

    @Column(name = "token", nullable = false)
    private String token;
}
