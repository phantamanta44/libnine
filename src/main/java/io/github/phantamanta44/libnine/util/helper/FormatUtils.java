package io.github.phantamanta44.libnine.util.helper;

public class FormatUtils {

    private static final String[] SI_PREFIXES = new String[] {null, "k", "M", "G", "T", "P", "E"};
    private static final String[] SI_PREFIXES_FP = new String[] {
            "n", "\u03bc", "m", "", "k", "M", "G", "T", "P", "E"
    };

    public static String formatSI(int num, String unit) {
        if (num == 0) return "0 " + unit;
        int magnitude = (int)Math.floor(Math.log10(Math.abs(num)) / 3);
        if (magnitude == 0) return num + " " + unit;
        return String.format("%.2f %s%s", num / Math.pow(10, magnitude * 3), SI_PREFIXES[magnitude], unit);
    }

    public static String formatSI(long num, String unit) {
        if (num == 0) return "0 " + unit;
        int magnitude = (int)Math.floor(Math.log10(Math.abs(num)) / 3);
        if (magnitude == 0) return num + " " + unit;
        return String.format("%.2f %s%s", num / Math.pow(10, magnitude * 3), SI_PREFIXES[magnitude], unit);
    }

    public static String formatSI(double num, String unit) {
        long val = Double.valueOf(Math.round(num * 1e9D)).longValue();
        if (val == 0) return "0 " + unit;
        int magnitude = (int)Math.floor(Math.log10(Math.abs(val)) / 3);
        return String.format("%.2f %s%s", num / Math.pow(10, magnitude * 3), SI_PREFIXES_FP[magnitude], unit);
    }

    public static String formatClassName(Class<?> clazz) {
        return clazz.getName().substring(clazz.getName().lastIndexOf(".") + 1).replace('$', '_');
    }

    public static String toTitleCase(String a) {
        return a.substring(0, 1).toUpperCase() + a.substring(1);
    }
}
