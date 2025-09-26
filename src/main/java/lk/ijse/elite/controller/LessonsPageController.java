package lk.ijse.elite.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.util.StringConverter;
import lk.ijse.elite.config.FactoryConfiguration;
import lk.ijse.elite.dto.LessonDTO;
import lk.ijse.elite.entity.Course;
import lk.ijse.elite.entity.Instructor;
import lk.ijse.elite.entity.Lesson;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class LessonsPageController implements Initializable {

    // Form
    public Label lblId;
    public ComboBox<Course> cmbCourse;
    public ComboBox<Instructor> cmbInstructor;
    public DatePicker dpDate;
    public TextField txtTime;           // HH:mm
    public TextField txtDuration;       // minutes
    public TextField txtLocation;

    // Table
    public TableView<LessonDTO> tblLesson;
    public TableColumn<LessonDTO, String> colId;
    public TableColumn<LessonDTO, String> colCourse;
    public TableColumn<LessonDTO, String> colInstructor;
    public TableColumn<LessonDTO, LocalDate> colDate;
    public TableColumn<LessonDTO, LocalTime> colTime;
    public TableColumn<LessonDTO, Integer> colDuration;
    public TableColumn<LessonDTO, String> colLocation;

    // Buttons
    public Button btnSave, btnUpdate, btnDelete, btnReset;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Table bindings
        colId.setCellValueFactory(new PropertyValueFactory<>("lessonId"));
        colCourse.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        colInstructor.setCellValueFactory(new PropertyValueFactory<>("instructorName"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("durationMinutes"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));

        // Combo renderers
        cmbCourse.setConverter(new StringConverter<>() {
            @Override public String toString(Course c){ return c==null? "": c.getCourseId()+" - "+c.getCourseName(); }
            @Override public Course fromString(String s){ return null; }
        });
        cmbInstructor.setConverter(new StringConverter<>() {
            @Override public String toString(Instructor i){ return i==null? "": i.getInstructorId()+" - "+i.getInstructorName(); }
            @Override public Instructor fromString(String s){ return null; }
        });

        try {
            loadCombos();
            resetPage();
        } catch (Exception e) {
            e.printStackTrace();
            error("Failed to initialize lessons page.");
        }
    }

    // ===== UI helpers =====
    private void resetPage() throws Exception {
        loadNextId();
        loadTableData();

        clearFields();
        btnSave.setDisable(false);
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
        tblLesson.getSelectionModel().clearSelection();
    }

    private void clearFields() {
        cmbCourse.setValue(null);
        cmbInstructor.setValue(null);
        dpDate.setValue(null);
        txtTime.clear();
        txtDuration.clear();
        txtLocation.clear();
    }

    private void info(String m){ new Alert(Alert.AlertType.INFORMATION, m).show(); }
    private void error(String m){ new Alert(Alert.AlertType.ERROR, m).show(); }

    // ===== Hibernate ops =====
    private void loadCombos() throws Exception {
        try (Session s = FactoryConfiguration.getInstance().getSession()) {
            List<Course> courses = s.createQuery("from Course c order by c.courseId", Course.class).getResultList();
            List<Instructor> instructors = s.createQuery("from Instructor i order by i.instructorId", Instructor.class).getResultList();
            cmbCourse.setItems(FXCollections.observableArrayList(courses));
            cmbInstructor.setItems(FXCollections.observableArrayList(instructors));
        }
    }

    private void loadTableData() throws Exception {
        try (Session s = FactoryConfiguration.getInstance().getSession()) {
            List<LessonDTO> rows = s.createQuery(
                    "select new lk.ijse.elite.dto.LessonDTO(" +
                            "l.lessonId, " +
                            "l.course.courseId, l.course.courseName, " +
                            "l.instructor.instructorId, l.instructor.instructorName, " +
                            "l.date, l.time, l.durationMinutes, l.location) " +
                            "from Lesson l order by l.lessonId", LessonDTO.class
            ).getResultList();

            tblLesson.setItems(FXCollections.observableArrayList(rows));
        }
    }

    private void loadNextId() throws Exception {
        try (Session s = FactoryConfiguration.getInstance().getSession()) {
            String last = s.createQuery("select l.lessonId from Lesson l order by l.lessonId desc", String.class)
                    .setMaxResults(1).uniqueResult();
            lblId.setText(nextIdFrom(last));
        }
    }

    private String nextIdFrom(String last) {
        if (last == null) return "L001";
        String d = last.replaceAll("\\D+", "");
        int n = d.isEmpty()? 0 : Integer.parseInt(d);
        return String.format("L%03d", n + 1);
    }

    private boolean saveLesson(LessonDTO dto) throws Exception {
        Transaction tx = null;
        try (Session s = FactoryConfiguration.getInstance().getSession()) {
            tx = s.beginTransaction();

            Course course = s.get(Course.class, dto.getCourseId());
            Instructor instructor = s.get(Instructor.class, dto.getInstructorId());
            if (course == null || instructor == null) throw new IllegalArgumentException("Select valid course and instructor.");

            Lesson l = new Lesson(
                    dto.getLessonId(),
                    course,
                    instructor,
                    dto.getDate(),
                    dto.getTime(),
                    dto.getDurationMinutes(),
                    dto.getLocation()
            );
            s.persist(l);
            tx.commit();
            return true;
        } catch (Exception ex) {
            if (tx != null) tx.rollback();
            throw ex;
        }
    }

    private boolean updateLesson(LessonDTO dto) throws Exception {
        Transaction tx = null;
        try (Session s = FactoryConfiguration.getInstance().getSession()) {
            tx = s.beginTransaction();

            Lesson l = s.get(Lesson.class, dto.getLessonId());
            if (l == null) { tx.rollback(); return false; }

            Course course = s.get(Course.class, dto.getCourseId());
            Instructor instructor = s.get(Instructor.class, dto.getInstructorId());
            if (course == null || instructor == null) throw new IllegalArgumentException("Select valid course and instructor.");

            l.setCourse(course);
            l.setInstructor(instructor);
            l.setDate(dto.getDate());
            l.setTime(dto.getTime());
            l.setDurationMinutes(dto.getDurationMinutes());
            l.setLocation(dto.getLocation());

            s.merge(l);
            tx.commit();
            return true;
        } catch (Exception ex) {
            if (tx != null) tx.rollback();
            throw ex;
        }
    }

    private boolean deleteLesson(String id) throws Exception {
        Transaction tx = null;
        try (Session s = FactoryConfiguration.getInstance().getSession()) {
            tx = s.beginTransaction();
            Lesson l = s.get(Lesson.class, id);
            if (l == null) { tx.commit(); return false; }
            s.remove(l);
            tx.commit();
            return true;
        } catch (Exception ex) {
            if (tx != null) tx.rollback();
            throw ex;
        }
    }

    // ===== Actions (wired to FXML) =====
    public void btnLessonSaveOnAction(ActionEvent e) {
        LessonDTO dto = extract();
        if (dto == null) return;

        try {
            if (saveLesson(dto)) {
                resetPage();
                info("Lesson saved successfully!");
            } else error("Failed to save lesson.");
        } catch (Exception ex) {
            ex.printStackTrace();
            error("Error while saving lesson.\n" + ex.getMessage());
        }
    }

    public void btnLessonUpdateOnAction(ActionEvent e) {
        LessonDTO dto = extract();
        if (dto == null) return;

        try {
            if (updateLesson(dto)) {
                resetPage();
                info("Lesson updated successfully!");
            } else error("Lesson not found.");
        } catch (Exception ex) {
            ex.printStackTrace();
            error("Error while updating lesson.\n" + ex.getMessage());
        }
    }

    public void btnLessonDeleteOnAction(ActionEvent e) {
        Optional<ButtonType> r = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete this lesson?", ButtonType.YES, ButtonType.NO).showAndWait();
        if (r.isPresent() && r.get() == ButtonType.YES) {
            try {
                if (deleteLesson(lblId.getText())) {
                    resetPage();
                    info("Lesson deleted.");
                } else error("Lesson not found.");
            } catch (Exception ex) {
                ex.printStackTrace();
                error("Error while deleting lesson.\n" + ex.getMessage());
            }
        }
    }

    public void btnLessonResetOnAction(ActionEvent e) {
        try { resetPage(); } catch (Exception ex) { ex.printStackTrace(); error("Failed to reset."); }
    }

    public void onClickTable(MouseEvent e) {
        LessonDTO d = tblLesson.getSelectionModel().getSelectedItem();
        if (d == null) return;

        lblId.setText(d.getLessonId());
        // select matching course/instructor in combos
        cmbCourse.getItems().stream().filter(c -> c.getCourseId().equals(d.getCourseId())).findFirst().ifPresent(cmbCourse::setValue);
        cmbInstructor.getItems().stream().filter(i -> i.getInstructorId().equals(d.getInstructorId())).findFirst().ifPresent(cmbInstructor::setValue);
        dpDate.setValue(d.getDate());
        txtTime.setText(d.getTime() == null ? "" : d.getTime().format(TIME_FMT));
        txtDuration.setText(d.getDurationMinutes() == null ? "" : d.getDurationMinutes().toString());
        txtLocation.setText(d.getLocation());

        btnSave.setDisable(true);
        btnUpdate.setDisable(true); // enable only if you want strict edit flow
        btnDelete.setDisable(false);
        // If you prefer editable immediately:
        btnUpdate.setDisable(false);
    }

    // ===== Extract & validate =====
    private LessonDTO extract() {
        String id = lblId.getText() == null ? "" : lblId.getText().trim();
        Course course = cmbCourse.getValue();
        Instructor instructor = cmbInstructor.getValue();
        LocalDate date = dpDate.getValue();
        String timeText = txtTime.getText() == null ? "" : txtTime.getText().trim();
        String durText = txtDuration.getText() == null ? "" : txtDuration.getText().trim();
        String location = txtLocation.getText() == null ? "" : txtLocation.getText().trim();

        if (id.isBlank() || course == null || instructor == null || date == null ||
                timeText.isBlank() || durText.isBlank() || location.isBlank()) {
            error("Please fill all fields.");
            return null;
        }

        LocalTime time;
        try { time = LocalTime.parse(timeText, TIME_FMT); }
        catch (Exception ex) { error("Time must be in HH:mm format."); return null; }

        int minutes;
        try { minutes = Integer.parseInt(durText); if (minutes <= 0) throw new NumberFormatException(); }
        catch (Exception ex) { error("Duration must be a positive number of minutes."); return null; }

        return new LessonDTO(
                id,
                course.getCourseId(), course.getCourseName(),
                instructor.getInstructorId(), instructor.getInstructorName(),
                date, time, minutes, location
        );
    }
}
