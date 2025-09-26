package lk.ijse.elite.dto;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AppUserDTO {
    private String userId;
    private String userName;
    private String password;
    private String email;
    private String userRole;
}
