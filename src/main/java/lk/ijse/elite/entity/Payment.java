package lk.ijse.elite.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "Payment")
@Table(name = "payment")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @ToString
public class Payment implements Serializable {

    @Id
    @Column(name = "payment_id", length = 12, nullable = false)
    private String paymentId;   // e.g. P001

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_id", nullable = false)
    @ToString.Exclude
    private Enrollment enrollment;

    @Column(name = "paid_date", nullable = false)
    private LocalDate paidDate;

    @Column(name = "amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "method", length = 20, nullable = false)
    private String method;      // e.g. CASH | CARD | ONLINE | UPFRONT

    @Column(name = "note", length = 255)
    private String note;
}
