package lk.ijse.elite.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity(name = "Lesson")
@Table(name="lesson",
        uniqueConstraints={
                @UniqueConstraint(columnNames={"instructor_id","start_time"}),
                @UniqueConstraint(columnNames={"student_id","start_time"})
        })
public class Lesson {
    @Id
    @Column(name="lesson_id", length=36) private String lessonId;

    @ManyToOne @JoinColumn(name="student_id", nullable=false)   private Student student;
    @ManyToOne @JoinColumn(name="course_id",  nullable=false)   private Course course;
    @ManyToOne @JoinColumn(name="instructor_id", nullable=false)private Instructor instructor;

    @Column(name="start_time", nullable=false) private LocalDateTime startTime;
    @Column(name="end_time",   nullable=false) private LocalDateTime endTime;
    @Column(name="status", length=20, nullable=false) private String status; // BOOKED/COMPLETED/CANCELLED
}
