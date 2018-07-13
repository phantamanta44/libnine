package io.github.phantamanta44.libnine.wsd;

public interface IWSDIdentity<T extends L9WSD> {

    String getIdentifier();

    Class<T> getType();

    String getPrefix();

}
