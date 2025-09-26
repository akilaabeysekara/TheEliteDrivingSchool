package lk.ijse.elite.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "Student")
@Table(name = "student")
@Data
@NoArgsConstructor
@AllArgsConstructor // now 7-arg (includes enrollments)
public class Student implements Serializable {

    @Id
    @Column(name = "student_id", length = 36)
    private String studentId;

    @Column(name = "name", nullable = false)
    private String studentName;

    @Column(name = "nic", nullable = false, unique = true)
    private String studentNic;

    @Column(name = "email", nullable = false, unique = true)
    private String studentEmail;

    @Column(name = "phone_no", nullable = false)
    private String studentPhone;

    @Column(name = "address", nullable = false)
    private String studentAddress;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Enrollment> enrollments = new HashSet<>();

    public Student(String studentId, String studentName, String studentNic,
                   String studentEmail, String studentPhone, String studentAddress) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentNic = studentNic;
        this.studentEmail = studentEmail;
        this.studentPhone = studentPhone;
        this.studentAddress = studentAddress;
    }

    public void addEnrollment(Enrollment e) {
        enrollments.add(e);
        e.setStudent(this);
    }
    public void removeEnrollment(Enrollment e) {
        enrollments.remove(e);
        e.setStudent(null);
    }
}
