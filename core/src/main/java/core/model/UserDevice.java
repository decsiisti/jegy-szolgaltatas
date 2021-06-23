package core.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "UserDevice")
public class UserDevice implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name="userId", nullable = false)
    private User user;

    @Column(name = "deviceHash", nullable = false)
    private String deviceHash;

    public UserDevice () {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getDeviceHash() {
        return deviceHash;
    }

    public void setDeviceHash(String deviceHash) {
        this.deviceHash = deviceHash;
    }

    @Override
    public String toString() {
        return "UserDevice{" +
                "id=" + id +
                ", user=" + user +
                ", deviceHash='" + deviceHash + '\'' +
                '}';
    }
}
