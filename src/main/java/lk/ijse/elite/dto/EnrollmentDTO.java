package lk.ijse.elite.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @ToString
@NoArgsConstructor @AllArgsConstructor
public class EnrollmentDTO {
    private String enrollmentId;
    private String studentId;
    private String studentName;  // for table view
    private String courseId;
    private String courseName;   // for table view
    private LocalDate regDate;
    private BigDecimal upfrontAmount;
    private String status;
}
