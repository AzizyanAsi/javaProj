package net.idonow.common.data;

import java.util.regex.Pattern;

@SuppressWarnings("All")
public abstract class RegexPatterns {
    public static final String REGX_PASSWORD = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{6,24}$";


    // DUMMY METHOD - FOR IDE HIGHLIGHTING ONLY
    private static void highlight() {
        Pattern rp = Pattern.compile(REGX_PASSWORD);
    }
}
