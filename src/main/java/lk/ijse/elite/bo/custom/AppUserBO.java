package lk.ijse.elite.bo.custom;

import lk.ijse.elite.bo.SuperBO;
import lk.ijse.elite.dto.AppUserDTO;

import java.util.List;

public interface AppUserBO extends SuperBO {
    // CRUD for User
    String getNextId() throws Exception;

    List<AppUserDTO> getAllUsers() throws Exception;

    void saveUser(AppUserDTO dto) throws Exception;

    void updateUser(AppUserDTO dto) throws Exception;

    boolean deleteUser(String userId) throws Exception;

    // Forgot-password / login
    boolean existsByEmail(String email);

    String generateRandomPassword();

    boolean updatePassword(String email, String newPassword);

    boolean verifyLogin(String email, String rawPassword);

    AppUserDTO getUserByRole(String role) throws Exception;
}