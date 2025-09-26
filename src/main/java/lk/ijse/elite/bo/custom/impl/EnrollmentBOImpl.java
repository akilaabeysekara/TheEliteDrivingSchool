package lk.ijse.elite.bo.custom.impl;

import lk.ijse.elite.bo.custom.EnrollmentBO;
import lk.ijse.elite.config.FactoryConfiguration;
import lk.ijse.elite.dto.EnrollmentDTO;
import lk.ijse.elite.entity.Course;
import lk.ijse.elite.entity.Enrollment;
import lk.ijse.elite.entity.Student;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class EnrollmentBOImpl implements EnrollmentBO {

    @Override
    public String getNextId() throws Exception {
        try (Session s = FactoryConfiguration.getInstance().getSession()) {
            String last = s.createQuery(
                    "select e.enrollmentId from Enrollment e order by e.enrollmentId desc",
                    String.class
            ).setMaxResults(1).uniqueResult();
            if (last == null) return "ENR0001";
            String num = last.replaceAll("\\D+", "");
            int n = num.isEmpty() ? 0 : Integer.parseInt(num);
            return String.format("ENR%04d", n + 1);
        }
    }

    @Override
    public List<EnrollmentDTO> getAll() throws Exception {
        try (Session s = FactoryConfiguration.getInstance().getSession()) {
            return s.createQuery(
                    "select new lk.ijse.elite.dto.EnrollmentDTO(" +
                            "e.enrollmentId, st.studentId, st.studentName, " +
                            "c.courseId, c.courseName, e.regDate, e.upfrontAmount, e.status) " +
                            "from Enrollment e " +
                            "join e.student st " +
                            "join e.course  c " +
                            "order by e.enrollmentId",
                    EnrollmentDTO.class
            ).getResultList();
        }
    }

    @Override
    public boolean save(EnrollmentDTO dto) throws Exception {
        Transaction tx = null;
        try (Session s = FactoryConfiguration.getInstance().getSession()) {
            tx = s.beginTransaction();

            // Validate refs
            Student st = s.get(Student.class, dto.getStudentId());
            Course  c  = s.get(Course.class, dto.getCourseId());
            if (st == null || c == null) throw new IllegalArgumentException("Invalid student/course");

            // Prevent duplicate pair
            Long dup = s.createQuery(
                            "select count(e) from Enrollment e where e.student.studentId=:sid and e.course.courseId=:cid",
                            Long.class
                    ).setParameter("sid", dto.getStudentId())
                    .setParameter("cid", dto.getCourseId())
                    .uniqueResult();
            if (dup != null && dup > 0) throw new IllegalStateException("Student already enrolled for this course.");

            // Assign ID if missing
            String id = (dto.getEnrollmentId() == null || dto.getEnrollmentId().isBlank())
                    ? getNextId() : dto.getEnrollmentId();

            Enrollment e = new Enrollment(
                    id, st, c, dto.getRegDate(), dto.getUpfrontAmount(), dto.getStatus()
            );
            s.persist(e);
            tx.commit();
            return true;
        } catch (Exception ex) {
            if (tx != null) tx.rollback();
            throw ex;
        }
    }

    @Override
    public boolean update(EnrollmentDTO dto) throws Exception {
        Transaction tx = null;
        try (Session s = FactoryConfiguration.getInstance().getSession()) {
            tx = s.beginTransaction();

            Enrollment e = s.get(Enrollment.class, dto.getEnrollmentId());
            if (e == null) { tx.rollback(); return false; }

            // allow changing student/course with duplicate check
            if (!e.getStudent().getStudentId().equals(dto.getStudentId())
                    || !e.getCourse().getCourseId().equals(dto.getCourseId())) {

                Long dup = s.createQuery(
                                "select count(en) from Enrollment en where en.student.studentId=:sid and en.course.courseId=:cid and en.enrollmentId<>:id",
                                Long.class
                        ).setParameter("sid", dto.getStudentId())
                        .setParameter("cid", dto.getCourseId())
                        .setParameter("id", dto.getEnrollmentId())
                        .uniqueResult();
                if (dup != null && dup > 0) throw new IllegalStateException("Student already enrolled for this course.");

                Student st = s.get(Student.class, dto.getStudentId());
                Course  c  = s.get(Course.class,  dto.getCourseId());
                if (st == null || c == null) throw new IllegalArgumentException("Invalid student/course");

                e.setStudent(st);
                e.setCourse(c);
            }

            e.setRegDate(dto.getRegDate());
            e.setUpfrontAmount(dto.getUpfrontAmount());
            e.setStatus(dto.getStatus());

            s.merge(e);
            tx.commit();
            return true;
        } catch (Exception ex) {
            if (tx != null) tx.rollback();
            throw ex;
        }
    }

    @Override
    public boolean delete(String enrollmentId) throws Exception {
        Transaction tx = null;
        try (Session s = FactoryConfiguration.getInstance().getSession()) {
            tx = s.beginTransaction();
            Enrollment e = s.get(Enrollment.class, enrollmentId);
            if (e == null) { tx.commit(); return false; }
            s.remove(e);
            tx.commit();
            return true;
        } catch (Exception ex) {
            if (tx != null) tx.rollback();
            throw ex;
        }
    }
}
