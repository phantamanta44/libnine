package io.github.phantamanta44.libnine.util.helper;

public class FormatUtils {

    private static final String[] SI_PREFIXES = new String[] {"", "k", "M", "G", "T", "P", "E"};

    public static String formatSI(int num, String unit) {
        if (num == 0) return num + " " + unit;
        int magnitude = (int)Math.floor(Math.log10(Math.abs(num)) / 3);
        if (magnitude == 0) return num + " " + unit;
        return String.format("%.2f %s%s", num / Math.pow(10, magnitude * 3), SI_PREFIXES[magnitude], unit);
    }

    public static String formatSI(long num, String unit) {
        if (num == 0) return num + " " + unit;
        int magnitude = (int)Math.floor(Math.log10(Math.abs(num)) / 3);
        if (magnitude == 0) return num + " " + unit;
        return String.format("%.2f %s%s", num / Math.pow(10, magnitude * 3), SI_PREFIXES[magnitude], unit);
    }

    public static String formatClassName(Class<?> clazz) {
        return clazz.getName().substring(clazz.getName().lastIndexOf(".") + 1).replace('$', '_');
    }

    public static String toTitleCase(String a) {
        return a.substring(0, 1).toUpperCase() + a.substring(1);
    }
}
