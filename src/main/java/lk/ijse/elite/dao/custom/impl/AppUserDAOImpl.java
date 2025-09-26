package lk.ijse.elite.dao.custom.impl;

import lk.ijse.elite.config.FactoryConfiguration;
import lk.ijse.elite.dao.custom.AppUserDAO;
import lk.ijse.elite.entity.AppUser;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AppUserDAOImpl implements AppUserDAO {
    private final FactoryConfiguration factoryConfiguration =
            FactoryConfiguration.getInstance();

    @Override
    public boolean save(Object dto) throws SQLException {
        Session session = factoryConfiguration.getSession();
        Transaction transaction = session.beginTransaction();
        try {
            AppUser entity = (AppUser) dto;
            session.persist(entity);
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        } finally {
            session.close();
        }
    }

    @Override
    public boolean update(Object dto) throws SQLException {
        Session session = factoryConfiguration.getSession();
        Transaction transaction = session.beginTransaction();
        try {
            AppUser entity = (AppUser) dto;
            session.merge(entity);
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        } finally {
            session.close();
        }
    }

    @Override
    public String getNextId() throws SQLException {
        Session session = factoryConfiguration.getSession();
        try {
            String lastId = getLastId();
            if (lastId == null) {
                return "U001";
            }
            int newId = Integer.parseInt(lastId.replace("U", "")) + 1;
            return String.format("U%03d", newId);
        } finally {
            session.close();
        }
    }

    @Override
    public String getLastId() throws SQLException {
        Session session = factoryConfiguration.getSession();
        try {
            Query<String> query = session.createQuery(
                    "SELECT a.userId FROM AppUser a ORDER BY a.userId DESC",
                    String.class
            ).setMaxResults(1);
            List<String> result = query.list();
            return result.isEmpty() ? null : result.get(0);
        } finally {
            session.close();
        }
    }

    @Override
    public Optional findById(String selectedId) throws SQLException {
        Session session = factoryConfiguration.getSession();
        try {
            AppUser appUser = session.get(AppUser.class, selectedId);
            return Optional.ofNullable(appUser);
        } finally {
            session.close();
        }
    }

    @Override
    public ArrayList<String> getAllIds() throws SQLException {
        Session session = factoryConfiguration.getSession();
        try {
            Query<String> query = session.createQuery(
                    "SELECT a.userId FROM AppUser a",
                    String.class
            );
            return new ArrayList<>(query.list());
        } finally {
            session.close();
        }
    }

    @Override
    public boolean delete(String ID) throws SQLException {
        Session session = factoryConfiguration.getSession();
        Transaction transaction = session.beginTransaction();
        try {
            AppUser appUser = session.get(AppUser.class, ID);
            if (appUser != null) {
                session.remove(appUser);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            transaction.rollback();
            return false;
        } finally {
            session.close();
        }
    }

    @Override
    public List getAll() throws SQLException {
        Session session = factoryConfiguration.getSession();
        try {
            Query<AppUser> query = session.createQuery(
                    "FROM AppUser",
                    AppUser.class
            );
            return query.list();
        } finally {
            session.close();
        }
    }
}