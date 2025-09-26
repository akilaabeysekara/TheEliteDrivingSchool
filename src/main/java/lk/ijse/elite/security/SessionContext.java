package lk.ijse.elite.security;

public final class SessionContext {
    private static String role;
    private SessionContext() {}
    public static void setRole(String r){ role = r; }
    public static String getRole(){ return role; }
}
