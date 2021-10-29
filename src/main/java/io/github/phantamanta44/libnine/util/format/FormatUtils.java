package io.github.phantamanta44.libnine.util.format;

import io.github.phantamanta44.libnine.util.math.MathUtils;
import net.minecraft.nbt.*;
import net.minecraftforge.common.util.Constants;

public class FormatUtils {

    private static final String[] SI_PREFIXES = new String[] {null, "k", "M", "G", "T", "P", "E"};
    private static final String[] SI_PREFIXES_FP = new String[] {
            "n", "\u03bc", "m", "", "k", "M", "G", "T", "P", "E"
    };

    @SuppressWarnings("DuplicatedCode")
    public static String formatSI(int num, String unit) {
        if (num == 0) return "0 " + unit;
        int magnitude = (int)Math.floor(Math.log10(Math.abs(num)) / 3);
        if (magnitude == 0) return num + " " + unit;
        return String.format("%.2f %s%s", num / Math.pow(10, magnitude * 3), SI_PREFIXES[magnitude], unit);
    }

    @SuppressWarnings("DuplicatedCode")
    public static String formatSI(long num, String unit) {
        if (num == 0L) return "0 " + unit;
        int magnitude = (int)Math.floor(Math.log10(Math.abs(num)) / 3);
        if (magnitude == 0) return num + " " + unit;
        return String.format("%.2f %s%s", num / Math.pow(10, magnitude * 3), SI_PREFIXES[magnitude], unit);
    }

    public static String formatSI(double num, String unit) {
        if (MathUtils.fpEquals(num, 0D)) return "0 " + unit;
        double val = num * 1e9D;
        int magnitude = Math.max((int)Math.floor(Math.log10(Math.abs(val)) / 3), 0);
        return String.format("%.2f %s%s", val / Math.pow(10, magnitude * 3), SI_PREFIXES_FP[magnitude], unit);
    }

    public static String formatPercentage(float percent) {
        return String.format("%.1f%%", percent * 100);
    }

    public static String formatPercentage(double percent) {
        return String.format("%.1f%%", percent * 100);
    }

    public static String formatClassName(Class<?> clazz) {
        return clazz.getName().substring(clazz.getName().lastIndexOf(".") + 1).replace('$', '_');
    }

    public static String toTitleCase(String a) {
        return a.substring(0, 1).toUpperCase() + a.substring(1);
    }

    public static String formatNbt(NBTBase tag) {
        TabulatedStringBuffer buf = new TabulatedStringBuffer();
        formatNbt(buf, tag);
        return buf.toString();
    }

    public static void formatNbt(TabulatedStringBuffer buf, NBTBase tag) {
        switch (tag.getId()) {
            case Constants.NBT.TAG_BYTE:
                buf.append(String.format("%2xb", ((NBTTagByte)tag).getByte()));
                break;
            case Constants.NBT.TAG_SHORT:
                buf.append(((NBTTagShort)tag).getShort() + "s");
                break;
            case Constants.NBT.TAG_INT:
                buf.append(((NBTTagInt)tag).getInt() + "i");
                break;
            case Constants.NBT.TAG_LONG:
                buf.append(((NBTTagLong)tag).getLong() + "j");
                break;
            case Constants.NBT.TAG_FLOAT:
                buf.append(String.format("%.4ff", ((NBTTagFloat)tag).getFloat()));
                break;
            case Constants.NBT.TAG_DOUBLE:
                buf.append(String.format("%.4fd", ((NBTTagDouble)tag).getDouble()));
                break;
            case Constants.NBT.TAG_BYTE_ARRAY:
                buf.append("[");
                for (byte n : ((NBTTagByteArray)tag).getByteArray()) {
                    buf.append(String.format("%2xb", n));
                }
                buf.append("]");
                break;
            case Constants.NBT.TAG_INT_ARRAY:
                buf.append("[");
                for (int n : ((NBTTagIntArray)tag).getIntArray()) {
                    buf.append(n + "i");
                }
                buf.append("]");
                break;
            case Constants.NBT.TAG_LONG_ARRAY:
                buf.append(tag.toString()); // lol wtf
                break;
            case Constants.NBT.TAG_STRING:
                buf.append(((NBTTagString)tag).getString());
                break;
            case Constants.NBT.TAG_LIST:
                buf.append("[").indent();
                ((NBTTagList)tag).forEach(subTag -> {
                    buf.newLine();
                    formatNbt(buf, subTag);
                });
                buf.outdent().appendLine("]");
                break;
            case Constants.NBT.TAG_COMPOUND:
                buf.append("{").indent();
                NBTTagCompound compound = (NBTTagCompound)tag;
                compound.getKeySet().forEach(key -> {
                    buf.appendLine(key).append(": ");
                    formatNbt(buf, compound.getTag(key));
                });
                buf.outdent().appendLine("}");
                break;
        }
    }

}
