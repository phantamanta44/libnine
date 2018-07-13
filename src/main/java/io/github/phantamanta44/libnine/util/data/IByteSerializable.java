package io.github.phantamanta44.libnine.util.data;

public interface IByteSerializable {

    void serBytes(ByteUtils.Writer data);

    void deserBytes(ByteUtils.Reader data);

}
