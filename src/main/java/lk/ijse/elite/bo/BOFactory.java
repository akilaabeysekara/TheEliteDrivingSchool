package lk.ijse.elite.bo;

import lk.ijse.elite.bo.custom.impl.AppUserBOImpl;

public class BOFactory {
    private static BOFactory instance;

    private BOFactory() {}

    public static BOFactory getInstance() {
        if (instance == null) instance = new BOFactory();
        return instance;
    }

    public enum BOType { USER }

    @SuppressWarnings("unchecked")
    public <T extends SuperBO> T getBO(BOType type) {
        return switch (type) {
            case USER -> (T) new AppUserBOImpl();
        };
    }
}
