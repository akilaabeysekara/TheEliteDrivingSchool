package lk.ijse.elite.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class EnrollmentId implements Serializable {
    @Column(name="student_id") private String studentId;
    @Column(name="course_id")  private String courseId;
    // equals/hashCode
}
