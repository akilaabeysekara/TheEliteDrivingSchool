package lk.ijse.elite.bo.custom;

import lk.ijse.elite.bo.SuperBO;
import lk.ijse.elite.dto.StudentDTO;
import lk.ijse.elite.entity.Student;

import java.util.List;

public interface StudentBO extends SuperBO {
    // existing CRUD you may already have...
    String getNextId() throws Exception;
    List<StudentDTO> getAll() throws Exception;
    boolean save(StudentDTO dto) throws Exception;
    boolean update(StudentDTO dto) throws Exception;
    boolean delete(String id) throws Exception;

    // === HQL tasks ===
    List<Student>    findStudentsInAllCourses() throws Exception;
    List<StudentDTO> findStudentsInAllCoursesDTO() throws Exception;
    List<Student>    findAllWithEnrollmentsAndCourses() throws Exception;
}
