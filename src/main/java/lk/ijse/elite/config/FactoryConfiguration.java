package lk.ijse.elite.config;

import lk.ijse.elite.entity.AppUser;
import lk.ijse.elite.entity.Course;
import lk.ijse.elite.entity.Instructor;
import lk.ijse.elite.entity.Student;
import lk.ijse.elite.entity.Enrollment;
import lk.ijse.elite.entity.EnrollmentId;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class FactoryConfiguration {
    private static volatile FactoryConfiguration instance;
    private final SessionFactory sessionFactory;

    private FactoryConfiguration() {
        try {
            // Loads hibernate.cfg.xml from classpath
            Configuration cfg = new Configuration().configure();

            // ✅ Register ALL annotated classes explicitly
            cfg.addAnnotatedClass(AppUser.class);
            cfg.addAnnotatedClass(Student.class);
            cfg.addAnnotatedClass(Course.class);
            cfg.addAnnotatedClass(Instructor.class);

            // --- Enrollments (fixes "not an @Entity" error) ---
            cfg.addAnnotatedClass(Enrollment.class);
            cfg.addAnnotatedClass(EnrollmentId.class); // @Embeddable

            // If add more entities later, register them here as well:
            // cfg.addAnnotatedClass(Lesson.class);
            // cfg.addAnnotatedClass(Payment.class);
            // ...

            sessionFactory = cfg.buildSessionFactory();
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

    public void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }
}
