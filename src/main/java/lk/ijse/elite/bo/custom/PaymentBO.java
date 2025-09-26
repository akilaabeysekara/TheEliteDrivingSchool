package lk.ijse.elite.bo.custom;

import lk.ijse.elite.bo.SuperBO;
import lk.ijse.elite.dto.PaymentDTO;
import lk.ijse.elite.entity.Enrollment;

import java.util.List;

public interface PaymentBO extends SuperBO {
    String getNextId() throws Exception;
    List<PaymentDTO> getAllPayments() throws Exception;

    // pass the selected enrollmentId from the controller
    void save(PaymentDTO dto, String enrollmentId) throws Exception;
    void update(PaymentDTO dto, String enrollmentId) throws Exception;

    boolean delete(String paymentId) throws Exception;

    // for combo
    List<Enrollment> listEnrollmentsForCombo() throws Exception;
}
