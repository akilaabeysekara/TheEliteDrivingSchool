package lk.ijse.elite.util;

import lk.ijse.elite.config.FactoryConfiguration;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.sql.SQLException;

public class CrudUtil {



    public static <T> T execute(String sql, Object... obj) throws SQLException {
        Session session = FactoryConfiguration.getInstance().getCurrentSession();
        try {
            if (sql.toLowerCase().startsWith("select")) {
                Query query = session.createNativeQuery(sql);

                for (int i = 0; i < obj.length; i++) {
                    query.setParameter(i + 1, obj[i]);
                }

                return (T) query.getResultList();
            } else {
                Query query = session.createNativeQuery(sql);

                for (int i = 0; i < obj.length; i++) {
                    query.setParameter(i + 1, obj[i]);
                }

                int result = query.executeUpdate();
                return (T) (Boolean) (result > 0);
            }
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }
}