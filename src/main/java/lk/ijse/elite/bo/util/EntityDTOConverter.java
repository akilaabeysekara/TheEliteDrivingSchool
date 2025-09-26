package lk.ijse.elite.bo.util;

import lk.ijse.elite.dto.AppUserDTO;
import lk.ijse.elite.dto.StudentDTO;
import lk.ijse.elite.entity.AppUser;
import lk.ijse.elite.entity.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class EntityDTOConverter {
    private EntityDTOConverter() {} // utility class

    // -------------------- AppUser --------------------

    /** Entity -> DTO (exclude password by default) */
    public static AppUserDTO toDTO(AppUser e) {
        return toDTO(e, false);
    }

    /** Entity -> DTO with control over including the (hashed) password */
    public static AppUserDTO toDTO(AppUser e, boolean includePassword) {
        if (e == null) return null;
        return new AppUserDTO(
                e.getUserId(),
                e.getUserName(),
                includePassword ? nz(e.getPassword()) : "", // avoid exposing hash by default
                e.getEmail(),
                e.getUserRole()
        );
    }

    /** DTO -> Entity (do NOT hash here; let BO/service handle hashing) */
    public static AppUser toEntity(AppUserDTO d) {
        if (d == null) return null;
        AppUser e = new AppUser();
        e.setUserId(nz(d.getUserId()));
        e.setUserName(nz(d.getUserName()));
        e.setPassword(nz(d.getPassword())); // raw or hashed; caller decides
        e.setEmail(nz(d.getEmail()));
        e.setUserRole(nz(d.getUserRole()));
        return e;
    }

    public static List<AppUserDTO> toUserDTOs(List<AppUser> entities) {
        return toUserDTOs(entities, false);
    }

    public static List<AppUserDTO> toUserDTOs(List<AppUser> entities, boolean includePassword) {
        List<AppUserDTO> out = new ArrayList<>();
        if (entities == null) return out;
        for (AppUser e : entities) out.add(toDTO(e, includePassword));
        return out;
    }

    // -------------------- Student --------------------

    /** Entity -> DTO */
    public static StudentDTO toDTO(Student e) {
        if (e == null) return null;
        return new StudentDTO(
                e.getStudentId(),
                e.getStudentName(),
                e.getStudentNic(),
                e.getStudentEmail(),
                e.getStudentPhone(),
                e.getStudentAddress()
        );
    }

    /** DTO -> Entity */
    public static Student toEntity(Student d) {
        // overload guard: avoid accidental same-type call
        return d;
    }

    /** DTO -> Entity */
    public static Student toEntity(StudentDTO d) {
        if (d == null) return null;
        return new Student(
                nz(d.getStudentId()),
                nz(d.getStudentName()),
                nz(d.getStudentNic()),
                nz(d.getStudentEmail()),
                nz(d.getStudentPhone()),
                nz(d.getStudentAddress())
        );
    }

    public static List<StudentDTO> toStudentDTOs(List<Student> entities) {
        List<StudentDTO> out = new ArrayList<>();
        if (entities == null) return out;
        for (Student e : entities) out.add(toDTO(e));
        return out;
    }

    // -------------------- helpers --------------------
    private static String nz(String s) { return s == null ? "" : s; }
}
