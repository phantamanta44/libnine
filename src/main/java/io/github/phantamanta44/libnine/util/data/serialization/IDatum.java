package io.github.phantamanta44.libnine.util.data.serialization;

public interface IDatum<T> {

    static <T> IDatum<T> of(T value) {
        return new NonPrim<>(value);
    }
    
    static OfInt ofInt(int value) {
        return new OfInt(value);
    }

    static OfFloat ofFloat(float value) {
        return new OfFloat(value);
    }

    static OfDouble ofDouble(double value) {
        return new OfDouble(value);
    }

    static OfByte ofByte(byte value) {
        return new OfByte(value);
    }

    static OfShort ofShort(short value) {
        return new OfShort(value);
    }

    static OfLong ofLong(long value) {
        return new OfLong(value);
    }

    static OfBool ofBool(boolean value) {
        return new OfBool(value);
    }

    void set(T value);

    T get();

    class NonPrim<T> implements IDatum<T> {

        private T value;

        NonPrim(T value) {
            this.value = value;
        }

        public void set(T value) {
            this.value = value;
        }

        public T get() {
            return value;
        }

    }

    class OfInt implements IDatum<Integer> {
        
        private int value;

        OfInt(int value) {
            this.value = value;
        }

        @Override
        public void set(Integer value) {
            setInt(value);
        }
        
        public void setInt(int value) {
            this.value = value;
        }

        @Override
        public Integer get() {
            return getInt();
        }
        
        public int getInt() {
            return value;
        }

        public int preincrement(int offset) {
            return (value += offset) - offset;
        }

        public int preincrement() {
            return preincrement(1);
        }

        public int postincrement(int offset) {
            return value += offset;
        }

        public int postincrement() {
            return postincrement(1);
        }
        
    }

    class OfFloat implements IDatum<Float> {

        private float value;

        OfFloat(float value) {
            this.value = value;
        }

        @Override
        public void set(Float value) {
            setFloat(value);
        }

        public void setFloat(float value) {
            this.value = value;
        }

        @Override
        public Float get() {
            return getFloat();
        }

        public float getFloat() {
            return value;
        }
        
    }
    
    class OfDouble implements IDatum<Double> {

        private double value;

        OfDouble(double value) {
            this.value = value;
        }

        @Override
        public void set(Double value) {
            setDouble(value);
        }

        public void setDouble(double value) {
            this.value = value;
        }

        @Override
        public Double get() {
            return getDouble();
        }

        public double getDouble() {
            return value;
        }
        
    }
    
    class OfByte implements IDatum<Byte> {

        private byte value;

        OfByte(byte value) {
            this.value = value;
        }

        @Override
        public void set(Byte value) {
            setByte(value);
        }

        public void setByte(byte value) {
            this.value = value;
        }

        @Override
        public Byte get() {
            return getByte();
        }

        public byte getByte() {
            return value;
        }
        
    }
    
    class OfShort implements IDatum<Short> {

        private short value;

        OfShort(short value) {
            this.value = value;
        }

        @Override
        public void set(Short value) {
            setShort(value);
        }

        public void setShort(short value) {
            this.value = value;
        }

        @Override
        public Short get() {
            return getShort();
        }

        public short getShort() {
            return value;
        }

        public short preincrement(short offset) {
            return (short)((value += offset) - offset);
        }

        public short preincrement() {
            return preincrement((short)1);
        }

        public short postincrement(short offset) {
            return value += offset;
        }

        public short postincrement() {
            return postincrement((short)1);
        }
        
    }
    
    class OfLong implements IDatum<Long> {

        private long value;

        OfLong(long value) {
            this.value = value;
        }

        @Override
        public void set(Long value) {
            setLong(value);
        }

        public void setLong(long value) {
            this.value = value;
        }

        @Override
        public Long get() {
            return getLong();
        }

        public long getLong() {
            return value;
        }

        public long preincrement(long offset) {
            return (value += offset) - offset;
        }

        public long preincrement() {
            return preincrement(1);
        }

        public long postincrement(long offset) {
            return value += offset;
        }

        public long postincrement() {
            return postincrement(1);
        }
        
    }
    
    class OfBool implements IDatum<Boolean> {

        private boolean value;

        OfBool(boolean value) {
            this.value = value;
        }

        @Override
        public void set(Boolean value) {
            setBool(value);
        }

        public void setBool(boolean value) {
            this.value = value;
        }

        @Override
        public Boolean get() {
            return isTrue();
        }

        public boolean isTrue() {
            return value;
        }
        
    }
    
}
