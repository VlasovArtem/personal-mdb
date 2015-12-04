package com.vlasovartem.pmdb.entity;

import com.vlasovartem.pmdb.entity.enums.UserRole;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by artemvlasov on 04/12/15.
 */
@Document(collection = "users")
public class User {
    private String id;
    private String username;
    private String email;
    private String password;
    private UserRole role;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
