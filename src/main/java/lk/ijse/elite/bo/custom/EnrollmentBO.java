package lk.ijse.elite.bo.custom;

import lk.ijse.elite.bo.SuperBO;
import lk.ijse.elite.dto.EnrollmentDTO;
import java.util.List;

public interface EnrollmentBO extends SuperBO {
    String getNextId() throws Exception;
    List<EnrollmentDTO> getAll() throws Exception;
    boolean save(EnrollmentDTO dto) throws Exception;
    boolean update(EnrollmentDTO dto) throws Exception;
    boolean delete(String enrollmentId) throws Exception;
}
