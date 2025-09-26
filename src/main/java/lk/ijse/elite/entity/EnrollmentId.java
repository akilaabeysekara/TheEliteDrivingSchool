package lk.ijse.elite.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class EnrollmentId implements Serializable {
    @Column(name = "student_id", length = 36, nullable = false)
    private String studentId;

    @Column(name = "course_id", length = 10, nullable = false)
    private String courseId;
}
