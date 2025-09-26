package lk.ijse.elite.dto;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class StudentDTO {
    private String studentId;
    private String studentName;
    private String studentNic;
    private String studentEmail;
    private String studentPhone;
    private String studentAddress;
}