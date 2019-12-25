package io.github.phantamanta44.libnine.wsd;

public class WSDIdentity<T extends L9WSD> implements IWSDIdentity<T> {

    private final String id;
    private final Class<T> type;

    public WSDIdentity(String id, Class<T> type) {
        this.id = id;
        this.type = type;
    }

    @Override
    public String getIdentifier() {
        return id;
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public String getPrefix() {
        return "wsd";
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object o) {
        return o instanceof WSDIdentity && ((WSDIdentity)o).id.equals(id);
    }

}
