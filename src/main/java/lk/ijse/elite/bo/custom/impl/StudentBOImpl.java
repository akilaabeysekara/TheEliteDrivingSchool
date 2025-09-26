package lk.ijse.elite.bo.custom.impl;

import lk.ijse.elite.bo.custom.StudentBO;
import lk.ijse.elite.config.FactoryConfiguration;
import lk.ijse.elite.dto.StudentDTO;
import lk.ijse.elite.entity.Student;
import org.hibernate.Session;

import java.util.List;

public class StudentBOImpl implements StudentBO {

    @Override
    public String getNextId() throws Exception {
        return "";
    }

    @Override
    public List<StudentDTO> getAll() throws Exception {
        return List.of();
    }

    @Override
    public boolean save(StudentDTO dto) throws Exception {
        return false;
    }

    @Override
    public boolean update(StudentDTO dto) throws Exception {
        return false;
    }

    @Override
    public boolean delete(String id) throws Exception {
        return false;
    }

    @Override
    public List<Student> findStudentsInAllCourses() throws Exception {
        try (Session s = FactoryConfiguration.getInstance().getSession()) {
            return s.createQuery(
                    "select s " +
                            "from Student s " +
                            "join s.enrollments e " +
                            "group by s " +
                            "having count(distinct e.course.courseId) = (select count(c) from Course c)",
                    Student.class
            ).getResultList();
        }
    }

    @Override
    public List<StudentDTO> findStudentsInAllCoursesDTO() throws Exception {
        try (Session s = FactoryConfiguration.getInstance().getSession()) {
            return s.createQuery(
                    "select new lk.ijse.elite.dto.StudentDTO(" +
                            " s.studentId, s.studentName, s.studentNic, s.studentEmail, s.studentPhone, s.studentAddress) " +
                            "from Student s join s.enrollments e " +
                            "group by s " +
                            "having count(distinct e.course.courseId) = (select count(c) from Course c)",
                    StudentDTO.class
            ).getResultList();
        }
    }

    @Override
    public List<Student> findAllWithEnrollmentsAndCourses() throws Exception {
        try (Session s = FactoryConfiguration.getInstance().getSession()) {
            return s.createQuery(
                    "select distinct s " +
                            "from Student s " +
                            "left join fetch s.enrollments e " +
                            "left join fetch e.course",
                    Student.class
            ).getResultList();
        }
    }
}
