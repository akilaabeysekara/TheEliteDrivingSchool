package lk.ijse.elite.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor @ToString
public class LessonDTO {
    private String lessonId;

    private String courseId;
    private String courseName;

    private String instructorId;
    private String instructorName;

    private LocalDate date;
    private LocalTime time;
    private Integer durationMinutes;
    private String location;
}
