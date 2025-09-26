package lk.ijse.elite.dao;

import lk.ijse.elite.dao.custom.impl.AppUserDAOImpl;
import lk.ijse.elite.dao.custom.impl.StudentDAOImpl;

public class DAOFactory {
    private static DAOFactory daoFactory;

    private DAOFactory() {}

    public static DAOFactory getInstance() {
        if (daoFactory == null) {
            daoFactory = new DAOFactory();
        }
        return daoFactory;
    }

    public enum DAOType {
        USER, STUDENT
    }

    public SuperDAO getDAO(DAOType type) {
        return (SuperDAO) switch (type) {
            case USER -> new AppUserDAOImpl();
            case STUDENT -> new StudentDAOImpl();
        };
    }
}
