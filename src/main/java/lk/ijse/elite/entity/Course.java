package lk.ijse.elite.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity(name = "Course")
@Table(name = "course")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @ToString
public class Course implements Serializable {

    @Id
    @Column(name = "course_id", length = 10)
    private String courseId;            // e.g. C1001

    @Column(name = "name", nullable = false, unique = true)
    private String courseName;

    @Column(name = "duration", nullable = false)
    private String duration;            // e.g. "12 weeks", "3 months"

    @Column(name = "fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal fee;             // LKR amount
}
