package lk.ijse.elite.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PaymentDTO {
    private String paymentId;

    private String studentId;
    private String studentName;

    private String courseId;
    private String courseName;

    private LocalDate paidDate;
    private BigDecimal amount;
    private String method;
    private String note;
}
