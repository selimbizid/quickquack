package de.tudortmund.webtech2.quickquack.ejb.other;

public class GlobalTools {
    public static boolean nonEmptyStrings(String... strings) {
        for (String s : strings) {
            if (s == null || s.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
