package lk.ijse.elite.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity(name = "Instructor")
@Table(name = "instructor")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @ToString
public class Instructor implements Serializable {

    @Id
    @Column(name = "instructor_id", length = 36)
    private String instructorId;

    @Column(name = "name", nullable = false)
    private String instructorName;

    @Column(name = "nic", nullable = false, unique = true)
    private String instructorNic;

    @Column(name = "email", nullable = false, unique = true)
    private String instructorEmail;

    @Column(name = "phone_no", nullable = false)
    private String instructorPhone;

    @Column(name = "address", nullable = false)
    private String instructorAddress;
}
