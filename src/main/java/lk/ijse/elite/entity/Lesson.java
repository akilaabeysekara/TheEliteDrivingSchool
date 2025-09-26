package lk.ijse.elite.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity(name = "Lesson")
@Table(name = "lesson")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Lesson implements Serializable {

    @EqualsAndHashCode.Include
    @Id
    @Column(name = "lesson_id", length = 10, nullable = false)
    private String lessonId;            // e.g. L001

    // Tie a lesson to a course and an instructor
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    @ToString.Exclude
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instructor_id", nullable = false)
    @ToString.Exclude
    private Instructor instructor;

    @Column(name = "lesson_date", nullable = false)
    private LocalDate date;

    @Column(name = "lesson_time", nullable = false)
    private LocalTime time;

    @Column(name = "duration_min", nullable = false)
    private Integer durationMinutes;

    @Column(name = "location", nullable = false)
    private String location;
}
