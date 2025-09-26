package lk.ijse.elite.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "Enrollment")
@Table(
        name = "enrollment",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_enroll_student_course",
                columnNames = {"student_id", "course_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Enrollment implements Serializable {

    private static final long serialVersionUID = 1L;

    @EqualsAndHashCode.Include
    @Id
    @Column(name = "enrollment_id", length = 12, nullable = false)
    private String enrollmentId; // e.g. ENR0001

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @ToString.Exclude
    private Student student;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @ToString.Exclude
    private Course course;

    @Column(name = "reg_date", nullable = false)
    private LocalDate regDate;

    @Column(name = "upfront_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal upfrontAmount;

    @Column(name = "status", nullable = false, length = 20)
    private String status; // ACTIVE / COMPLETED / CANCELLED


    public Enrollment(String enrollmentId,
                      Student student,
                      Course course,
                      LocalDate regDate,
                      BigDecimal upfrontAmount,
                      String status) {
        this.enrollmentId = enrollmentId;
        this.student = student;
        this.course = course;
        this.regDate = regDate;
        this.upfrontAmount = upfrontAmount;
        this.status = status;
    }
}
