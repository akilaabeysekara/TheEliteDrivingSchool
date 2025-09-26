package lk.ijse.elite.config;

import lk.ijse.elite.entity.AppUser;
import lk.ijse.elite.entity.Course;
import lk.ijse.elite.entity.Student;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class FactoryConfiguration {
    private static volatile FactoryConfiguration instance;
    private final SessionFactory sessionFactory;

    private FactoryConfiguration() {
        try {
            Configuration configuration = new Configuration().configure();

            // Register annotated entities
            configuration.addAnnotatedClass(AppUser.class);
            configuration.addAnnotatedClass(Student.class);
            configuration.addAnnotatedClass(Course.class);

            sessionFactory = configuration.buildSessionFactory();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build SessionFactory", e);
        }
    }

    public static FactoryConfiguration getInstance() {
        if (instance == null) {
            synchronized (FactoryConfiguration.class) {
                if (instance == null) {
                    instance = new FactoryConfiguration();
                }
            }
        }
        return instance;
    }

    public Session getSession() {
        return sessionFactory.openSession();
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    // Optional: safely close SessionFactory at shutdown
    public void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }
}
