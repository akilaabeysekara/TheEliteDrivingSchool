package lk.ijse.elite.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "Course")
@Table(name = "course")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Course implements Serializable {

    private static final long serialVersionUID = 1L;

    @EqualsAndHashCode.Include
    @Id
    @Column(name = "course_id", length = 10, nullable = false)
    private String courseId;            // e.g. C1001

    @Column(name = "name", nullable = false, unique = true)
    private String courseName;

    @Column(name = "duration", nullable = false)
    private String duration;            // e.g. "12 weeks", "3 months"

    @Column(name = "fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal fee;             // LKR amount

    @ToString.Exclude
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Enrollment> enrollments = new HashSet<>();

    // Convenience helpers to keep both sides of the association in sync
    public void addEnrollment(Enrollment e) {
        if (e == null) return;
        enrollments.add(e);
        e.setCourse(this);
    }

    public void removeEnrollment(Enrollment e) {
        if (e == null) return;
        enrollments.remove(e);
        e.setCourse(null);
    }
}
