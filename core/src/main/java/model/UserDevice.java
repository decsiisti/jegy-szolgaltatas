package model;

import javax.persistence.*;

@Entity
@Table(name = "UserDevice")
public class UserDevice {
    @ManyToOne
    @JoinColumn(name="userId", nullable = false)
    private User user;

    @Column(name = "deviceHash", nullable = false)
    private String deviceHash;

    public UserDevice () {

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
}
