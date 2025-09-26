package lk.ijse.elite.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity(name="Payment")
@Table(name="payment")
public class Payment {
    @Id
    @Column(name="payment_id", length=36) private String paymentId;
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name="student_id", referencedColumnName="student_id"),
            @JoinColumn(name="course_id",  referencedColumnName="course_id")
    }) private Enrollment enrollment;

    @Column(name="paid_on", nullable=false) private LocalDateTime paidOn;
    @Column(name="amount",  nullable=false) private BigDecimal amount;
    @Column(name="method",  nullable=false, length=20) private String method; // CASH/CARD/TRANSFER
    @Column(name="status",  nullable=false, length=20) private String status; // PAID/PENDING/REFUNDED
}

