package lk.ijse.elite.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter
@ToString
@AllArgsConstructor @NoArgsConstructor
public class CourseDTO {
    private String courseId;
    private String courseName;
    private String duration;
    private BigDecimal fee;
}
