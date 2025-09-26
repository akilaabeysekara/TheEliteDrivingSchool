package lk.ijse.elite.bo.custom.impl;

import lk.ijse.elite.bo.custom.AppUserBO;
import lk.ijse.elite.config.FactoryConfiguration;
import lk.ijse.elite.dto.AppUserDTO;
import lk.ijse.elite.entity.AppUser;
import lk.ijse.elite.security.PasswordUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class AppUserBOImpl implements AppUserBO {

    private static final String ENTITY = AppUser.class.getName();
    private static final String PW_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%&*!";
    private static final int PW_LEN = 10;

    // ---------- CRUD ----------

    @Override
    public String getNextId() throws Exception {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            String last = session.createQuery(
                    "select u.userId from " + ENTITY + " u order by u.userId desc",
                    String.class
            ).setMaxResults(1).uniqueResult();

            if (last == null) return "U001";
            String digits = last.replaceAll("\\D+", "");
            int num = digits.isEmpty() ? 0 : Integer.parseInt(digits);
            return String.format("U%03d", num + 1);
        } catch (Exception e) {
            throw new Exception("Failed to generate next ID", e);
        }
    }

    @Override
    public List<AppUserDTO> getAllUsers() throws Exception {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            List<AppUser> entities = session.createQuery(
                    "from " + ENTITY + " u order by u.userId",
                    AppUser.class
            ).getResultList();

            List<AppUserDTO> list = new ArrayList<>();
            for (AppUser u : entities) {
                list.add(new AppUserDTO(
                        u.getUserId(),
                        u.getUserName(),
                        "",                 // never expose stored hash
                        u.getEmail(),
                        u.getUserRole()     // map role
                ));
            }
            return list;
        } catch (Exception e) {
            throw new Exception("Failed to fetch users", e);
        }
    }

    @Override
    public void saveUser(AppUserDTO dto) throws Exception {
        Transaction tx = null;
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            tx = session.beginTransaction();

            AppUser user = new AppUser();
            user.setUserId(dto.getUserId());
            user.setUserName(dto.getUserName());
            user.setEmail(dto.getEmail());
            user.setUserRole(defaultRole(dto.getUserRole()));
            user.setPassword(PasswordUtil.hash(dto.getPassword()));

            session.persist(user);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new Exception("Failed to save user", e);
        }
    }

    @Override
    public void updateUser(AppUserDTO dto) throws Exception {
        Transaction tx = null;
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            tx = session.beginTransaction();

            AppUser user = session.get(AppUser.class, dto.getUserId());
            if (user == null) {
                throw new Exception("User not found: " + dto.getUserId());
            }

            user.setUserName(dto.getUserName());
            user.setEmail(dto.getEmail());
            user.setUserRole(defaultRole(dto.getUserRole()));

            if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
                user.setPassword(PasswordUtil.hash(dto.getPassword()));
            }

            session.merge(user);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new Exception("Failed to update user", e);
        }
    }

    @Override
    public boolean deleteUser(String userId) throws Exception {
        Transaction tx = null;
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            tx = session.beginTransaction();
            AppUser user = session.get(AppUser.class, userId);
            if (user == null) {
                tx.commit();
                return false;
            }
            session.remove(user);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new Exception("Failed to delete user", e);
        }
    }

    // ---------- Forgot-password / Login ----------

    @Override
    public boolean existsByEmail(String email) {
        if (email == null) return false;
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            Long count = session.createQuery(
                            "select count(u) from " + ENTITY + " u where lower(u.email) = :e",
                            Long.class
                    ).setParameter("e", email.toLowerCase())
                    .uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String generateRandomPassword() {
        SecureRandom r = new SecureRandom();
        StringBuilder sb = new StringBuilder(PW_LEN);
        for (int i = 0; i < PW_LEN; i++) sb.append(PW_CHARS.charAt(r.nextInt(PW_CHARS.length())));
        return sb.toString();
    }

    @Override
    public boolean updatePassword(String email, String newPassword) {
        if (email == null || newPassword == null || newPassword.isBlank()) return false;

        Transaction tx = null;
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            tx = session.beginTransaction();

            AppUser user = session.createQuery(
                            "from " + ENTITY + " u where lower(u.email) = :e",
                            AppUser.class
                    ).setParameter("e", email.toLowerCase())
                    .setMaxResults(1)
                    .uniqueResult();

            if (user == null) {
                tx.commit();
                return false;
            }

            user.setPassword(PasswordUtil.hash(newPassword));
            session.merge(user);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean verifyLogin(String usernameOrEmail, String rawPassword) {
        if (usernameOrEmail == null || rawPassword == null) return false;

        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            String id = usernameOrEmail.trim().toLowerCase();

            String storedHash = session.createQuery(
                            "select u.password from " + ENTITY + " u " +
                                    "where lower(u.userName) = :id or lower(u.email) = :id",
                            String.class
                    ).setParameter("id", id)
                    .setMaxResults(1)
                    .uniqueResult();

            return storedHash != null && PasswordUtil.matches(rawPassword, storedHash);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public AppUserDTO getUserByRole(String role) throws Exception {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            AppUser user = session.createQuery(
                            "from " + ENTITY + " u where upper(u.userRole) = :role",
                            AppUser.class
                    ).setParameter("role", role.toUpperCase())
                    .setMaxResults(1)
                    .uniqueResult();

            if (user == null) return null;

            return new AppUserDTO(
                    user.getUserId(),
                    user.getUserName(),
                    user.getPassword(),
                    user.getEmail(),
                    user.getUserRole()
            );
        } catch (Exception e) {
            throw new Exception("Failed to fetch user by role: " + role, e);
        }
    }

    @Override
    public String findRoleByLoginId(String usernameOrEmail) throws Exception {
        if (usernameOrEmail == null) return null;

        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            String role = session.createQuery(
                            "select u.userRole from " + ENTITY + " u " +
                                    "where lower(u.userName) = :id or lower(u.email) = :id",
                            String.class
                    ).setParameter("id", usernameOrEmail.trim().toLowerCase())
                    .setMaxResults(1)
                    .uniqueResult();

            return role != null ? role : null;
        } catch (Exception e) {
            throw new Exception("Failed to find user role", e);
        }
    }


    // ---------- utils ----------

    private String defaultRole(String role) {
        return (role == null || role.isBlank()) ? "USER" : role.trim().toUpperCase();
    }
}
