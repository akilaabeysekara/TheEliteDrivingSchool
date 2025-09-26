package lk.ijse.elite.dto;

import lombok.*;

@Getter @Setter
@ToString
@AllArgsConstructor @NoArgsConstructor
public class InstructorDTO {
    private String instructorId;
    private String instructorName;
    private String instructorNic;
    private String instructorEmail;
    private String instructorPhone;
    private String instructorAddress;
}
