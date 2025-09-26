package lk.ijse.elite.security;

import at.favre.lib.crypto.bcrypt.BCrypt;

public final class PasswordUtil {
    private PasswordUtil() {}

    public static String hash(String raw) {
        return BCrypt.withDefaults().hashToString(12, raw.toCharArray());
    }

    public static boolean matches(String raw, String hash) {
        return BCrypt.verifyer().verify(raw.toCharArray(), hash).verified;
    }
}

