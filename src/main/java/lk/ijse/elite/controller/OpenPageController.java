package lk.ijse.elite.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import lk.ijse.elite.config.FactoryConfiguration;
import org.hibernate.Session;

import java.math.BigDecimal;
import java.text.NumberFormat;

public class OpenPageController {

    @FXML private Label lblTotalStudents;
    @FXML private Label lblTotalCourses;
    @FXML private Label lblTotalInstructors;
    @FXML private Label lblActiveEnrollments;
    @FXML private Label lblCompletedEnrollments;
    @FXML private Label lblTotalUpfront;

    @FXML
    public void initialize() {
        loadStats();
    }

    private void loadStats() {
        try (Session s = FactoryConfiguration.getInstance().getSession()) {
            Long students = s.createQuery("select count(st) from Student st", Long.class).uniqueResult();
            Long courses  = s.createQuery("select count(c)  from Course c",  Long.class).uniqueResult();
            Long instr    = s.createQuery("select count(i)  from Instructor i", Long.class).uniqueResult();

            Long active   = s.createQuery(
                    "select count(e) from Enrollment e where upper(e.status) = 'ACTIVE'",
                    Long.class).uniqueResult();

            Long completed = s.createQuery(
                    "select count(e) from Enrollment e where upper(e.status) = 'COMPLETED'",
                    Long.class).uniqueResult();

            BigDecimal upfront = s.createQuery(
                    "select coalesce(sum(e.upfrontAmount), 0) from Enrollment e",
                    BigDecimal.class).uniqueResult();

            NumberFormat nf = NumberFormat.getNumberInstance();
            lblTotalStudents.setText(nf.format(nz(students)));
            lblTotalCourses.setText(nf.format(nz(courses)));
            lblTotalInstructors.setText(nf.format(nz(instr)));
            lblActiveEnrollments.setText(nf.format(nz(active)));
            lblCompletedEnrollments.setText(nf.format(nz(completed)));
            lblTotalUpfront.setText(formatCurrency(upfront));
        } catch (Exception e) {
            lblTotalStudents.setText("-");
            lblTotalCourses.setText("-");
            lblTotalInstructors.setText("-");
            lblActiveEnrollments.setText("-");
            lblCompletedEnrollments.setText("-");
            lblTotalUpfront.setText("-");
            e.printStackTrace();
        }
    }

    private static long nz(Long v) { return v == null ? 0L : v; }
    private static String formatCurrency(BigDecimal v) {
        if (v == null) return "0.00";
        return String.format("%,.2f", v);
    }
}
