package lk.ijse.elite.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "student")
@AllArgsConstructor
@NoArgsConstructor
@Data
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
}