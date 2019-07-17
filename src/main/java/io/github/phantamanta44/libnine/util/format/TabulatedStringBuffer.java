package io.github.phantamanta44.libnine.util.format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TabulatedStringBuffer {

    private final List<StringBuilder> lines;
    private int indentSize = 0;
    private String indent = "";
    private boolean dirty = false;

    public TabulatedStringBuffer(List<String> initial) {
        this.lines = initial.stream().map(StringBuilder::new).collect(Collectors.toCollection(ArrayList::new));
    }

    public TabulatedStringBuffer(String[] initial) {
        this.lines = Arrays.stream(initial).map(StringBuilder::new).collect(Collectors.toCollection(ArrayList::new));
    }

    public TabulatedStringBuffer(String initial) {
        this(initial.split("\n"));
    }

    public TabulatedStringBuffer() {
        this.lines = new ArrayList<>();
    }

    public TabulatedStringBuffer append(String text) {
        if (!text.isEmpty()) {
            String[] newLines = text.split("\n");
            if (lines.isEmpty()) {
                lines.add(new StringBuilder(getIndent()).append(newLines[0]));
            } else {
                lines.get(lines.size() - 1).append(newLines[0]);
            }
            if (newLines.length > 1) {
                for (int i = 1; i < newLines.length; i++) {
                    appendLine(newLines[i]);
                }
            }
        }
        return this;
    }

    public TabulatedStringBuffer appendLine(String line) {
        lines.add(new StringBuilder(getIndent()).append(line));
        return this;
    }

    public TabulatedStringBuffer newLine() {
        lines.add(new StringBuilder(getIndent()));
        return this;
    }

    public TabulatedStringBuffer indent() {
        indentSize += 2;
        dirty = true;
        return this;
    }

    public TabulatedStringBuffer outdent() {
        indentSize = Math.max(indentSize - 2, 0);
        dirty = true;
        return this;
    }

    private String getIndent() {
        if (dirty) {
            int length = indent.length();
            if (indentSize > length) {
                StringBuilder buf = new StringBuilder(indent);
                while (length < indentSize) {
                    buf.append("  ");
                    length += 2;
                }
                indent = buf.toString();
            } else {
                indent = indent.substring(0, indentSize);
            }
            dirty = false;
        }
        return indent;
    }

    public Stream<String> getLines() {
        return lines.stream().map(StringBuilder::toString);
    }

    @Override
    public String toString() {
        return String.join("\n", lines);
    }

}
