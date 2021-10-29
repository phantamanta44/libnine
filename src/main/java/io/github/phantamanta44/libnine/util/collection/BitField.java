package io.github.phantamanta44.libnine.util.collection;

public class BitField {

    private final int length;
    private final byte[] field;

    public BitField(int length) {
        this.length = length;
        this.field = new byte[(int)Math.ceil(length / 8F)];
    }

    public byte[] getBackingByteArray() {
        return field;
    }

    public boolean get(int index) {
        checkIndex(index);
        return (field[index / 8] & (1 << (index % 8))) != 0;
    }

    public void set(int index, boolean value) {
        checkIndex(index);
        if (value) {
            field[index / 8] |= 1 << (index % 8);
        } else {
            field[index / 8] &= ~(1 << (index % 8));
        }
    }

    public void flip(int index) {
        checkIndex(index);
        field[index / 8] ^= 1 << (index % 8);
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException(
                    String.format("Index %d outside bitfield length of %d!", index, length));
        }
    }

}
