package lk.ijse.elite.bo;

import lk.ijse.elite.bo.custom.impl.AppUserBOImpl;
import lk.ijse.elite.bo.custom.impl.StudentBOImpl;
import lk.ijse.elite.bo.custom.impl.EnrollmentBOImpl;
import lk.ijse.elite.bo.custom.impl.PaymentBOImpl;

public class BOFactory {
    private static BOFactory instance;

    private BOFactory() {}

    public static BOFactory getInstance() {
        if (instance == null) instance = new BOFactory();
        return instance;
    }

    public enum BOType { USER, STUDENT, ENROLLMENT, PAYMENT }

    @SuppressWarnings("unchecked")
    public <T extends SuperBO> T getBO(BOType type) {
        return switch (type) {
            case USER        -> (T) new AppUserBOImpl();
            case STUDENT     -> (T) new StudentBOImpl();
            case ENROLLMENT  -> (T) new EnrollmentBOImpl();
            case PAYMENT     -> (T) new PaymentBOImpl();
        };
    }
}
