package lk.ijse.elite.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "app_user")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AppUser implements Serializable {

    @Id
    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(name = "user_name", nullable = false, unique = true, length = 80)
    private String userName;

    // BCrypt hashes ~60 chars; 72 is safe
    @Column(name = "password", nullable = false, length = 72)
    private String password;

    @Column(name = "email", nullable = false, unique = true, length = 120)
    private String email;

    @Column(name = "user_role", nullable = false, length = 30)
    private String userRole;
}
