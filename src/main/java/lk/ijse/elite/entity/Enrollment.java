package lk.ijse.elite.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name="enrollment")
public class Enrollment {
    @EmbeddedId private EnrollmentId id = new EnrollmentId();

    @ManyToOne @MapsId("studentId")
    @JoinColumn(name="student_id", nullable=false)
    private Student student;

    @ManyToOne @MapsId("courseId")
    @JoinColumn(name="course_id", nullable=false)
    private Course course;

    @Column(name="reg_date", nullable=false)
    private LocalDate regDate;

    @Column(name="upfront_amount", nullable=false)
    private BigDecimal upfrontAmount;

    @Column(name="status", nullable=false, length=20)
    private String status; // e.g., "ACTIVE","COMPLETED","CANCELLED"

    @OneToMany(mappedBy="course", cascade=CascadeType.ALL, orphanRemoval=true)
    private Set<Enrollment> enrollments = new HashSet<>();
}
