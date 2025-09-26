package lk.ijse.elite.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "Enrollment")
@Table(name = "enrollment")
public class Enrollment implements Serializable {

    @EmbeddedId
    private EnrollmentId id = new EnrollmentId();

    @ManyToOne(optional = false)              // owning side
    @MapsId("studentId")                      // maps to id.studentId
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(optional = false)              // owning side
    @MapsId("courseId")                       // maps to id.courseId
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "reg_date", nullable = false)
    private LocalDate regDate;

    @Column(name = "upfront_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal upfrontAmount;

    @Column(name = "status", nullable = false, length = 20)
    private String status; // e.g., ACTIVE / COMPLETED / CANCELLED

    public Enrollment() {}

    public Enrollment(Student student, Course course,
                      LocalDate regDate, BigDecimal upfrontAmount, String status) {
        setStudent(student);
        setCourse(course);
        this.regDate = regDate;
        this.upfrontAmount = upfrontAmount;
        this.status = status;
    }

    // --- getters/setters ---
    public EnrollmentId getId() { return id; }
    public void setId(EnrollmentId id) { this.id = id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) {
        this.student = student;
        if (student != null) {
            this.id.setStudentId(student.getStudentId());
        } else {
            this.id.setStudentId(null);
        }
    }

    public Course getCourse() { return course; }
    public void setCourse(Course course) {
        this.course = course;
        if (course != null) {
            this.id.setCourseId(course.getCourseId());
        } else {
            this.id.setCourseId(null);
        }
    }

    public LocalDate getRegDate() { return regDate; }
    public void setRegDate(LocalDate regDate) { this.regDate = regDate; }

    public BigDecimal getUpfrontAmount() { return upfrontAmount; }
    public void setUpfrontAmount(BigDecimal upfrontAmount) { this.upfrontAmount = upfrontAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
