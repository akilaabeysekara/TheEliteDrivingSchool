package lk.ijse.elite.bo.custom.impl;

import lk.ijse.elite.bo.custom.PaymentBO;
import lk.ijse.elite.config.FactoryConfiguration;
import lk.ijse.elite.dto.PaymentDTO;
import lk.ijse.elite.entity.Enrollment;
import lk.ijse.elite.entity.Payment;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class PaymentBOImpl implements PaymentBO {

    @Override
    public String getNextId() throws Exception {
        try (Session s = FactoryConfiguration.getInstance().getSession()) {
            String last = s.createQuery(
                    "select p.paymentId from Payment p order by p.paymentId desc",
                    String.class
            ).setMaxResults(1).uniqueResult();
            if (last == null) return "P001";
            String digits = last.replaceAll("\\D+", "");
            int n = digits.isEmpty() ? 0 : Integer.parseInt(digits);
            return String.format("P%03d", n + 1);
        }
    }

    @Override
    public List<PaymentDTO> getAllPayments() throws Exception {
        try (Session s = FactoryConfiguration.getInstance().getSession()) {
            // Enrollment now has a single PK and direct refs student/course
            return s.createQuery(
                    "select new lk.ijse.elite.dto.PaymentDTO(" +
                            "p.paymentId, " +
                            "st.studentId, st.studentName, " +
                            "c.courseId,  c.courseName, " +
                            "p.paidDate, p.amount, p.method, p.note) " +
                            "from Payment p " +
                            "join p.enrollment e " +
                            "join e.student st " +
                            "join e.course  c " +
                            "order by p.paymentId",
                    PaymentDTO.class
            ).getResultList();
        }
    }

    @Override
    public void save(PaymentDTO dto, String enrollmentId) throws Exception {
        Transaction tx = null;
        try (Session s = FactoryConfiguration.getInstance().getSession()) {
            tx = s.beginTransaction();

            Enrollment en = s.get(Enrollment.class, enrollmentId);
            if (en == null) throw new IllegalArgumentException("Invalid enrollment.");

            Payment p = new Payment(
                    dto.getPaymentId(),
                    en,
                    dto.getPaidDate(),
                    dto.getAmount(),
                    dto.getMethod(),
                    dto.getNote()
            );
            s.persist(p);
            tx.commit();
        } catch (Exception ex) {
            if (tx != null) tx.rollback();
            throw ex;
        }
    }

    @Override
    public void update(PaymentDTO dto, String enrollmentId) throws Exception {
        Transaction tx = null;
        try (Session s = FactoryConfiguration.getInstance().getSession()) {
            tx = s.beginTransaction();

            Payment p = s.get(Payment.class, dto.getPaymentId());
            if (p == null) throw new IllegalArgumentException("Payment not found.");

            Enrollment en = s.get(Enrollment.class, enrollmentId);
            if (en == null) throw new IllegalArgumentException("Invalid enrollment.");

            p.setEnrollment(en);
            p.setPaidDate(dto.getPaidDate());
            p.setAmount(dto.getAmount());
            p.setMethod(dto.getMethod());
            p.setNote(dto.getNote());

            s.merge(p);
            tx.commit();
        } catch (Exception ex) {
            if (tx != null) tx.rollback();
            throw ex;
        }
    }

    @Override
    public boolean delete(String paymentId) throws Exception {
        Transaction tx = null;
        try (Session s = FactoryConfiguration.getInstance().getSession()) {
            tx = s.beginTransaction();
            Payment p = s.get(Payment.class, paymentId);
            if (p == null) { tx.commit(); return false; }
            s.remove(p);
            tx.commit();
            return true;
        } catch (Exception ex) {
            if (tx != null) tx.rollback();
            throw ex;
        }
    }

    @Override
    public List<Enrollment> listEnrollmentsForCombo() throws Exception {
        try (Session s = FactoryConfiguration.getInstance().getSession()) {
            return s.createQuery(
                    "select e from Enrollment e " +
                            "join fetch e.student " +
                            "join fetch e.course " +
                            "order by e.enrollmentId",
                    Enrollment.class
            ).getResultList();
        }
    }
}
