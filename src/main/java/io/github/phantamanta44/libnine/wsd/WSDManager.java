package io.github.phantamanta44.libnine.wsd;

import io.github.phantamanta44.libnine.LibNine;
import io.github.phantamanta44.libnine.Virtue;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class WSDManager {

    private final Virtue virtue;
    private final Map<IWSDIdentity<? extends L9WSD>, Function<World, ? extends L9WSD>> factoryMap;

    public WSDManager(Virtue virtue) {
        this.virtue = virtue;
        this.factoryMap = new HashMap<>();
    }

    public <T extends L9WSD> void register(WSDIdentity<T> identity, Function<World, T> factory) {
        factoryMap.put(identity, factory);
    }

    public <T extends L9WSD> void register(WSDGlobalIdentity<T> identity, Supplier<T> factory) {
        factoryMap.put(identity, w -> factory.get());
    }

    @SuppressWarnings("unchecked")
    public <T extends L9WSD> T get(WSDIdentity<T> identity, World world) {
        return (T)get0(identity, world);
    }

    @SuppressWarnings("unchecked")
    public <T extends L9WSD> T get(WSDGlobalIdentity<T> identity) {
        return (T)get0(identity, LibNine.PROXY.getAnySidedWorld());
    }

    private L9WSD get0(IWSDIdentity<?> identity, World world) {
        String identifier = getIdentifier(identity);
        L9WSD data = (L9WSD)world.getPerWorldStorage().getOrLoadData(identity.getType(), identifier);
        if (data == null) {
            data = factoryMap.get(identity).apply(world);
            world.getPerWorldStorage().setData(identifier, data);
        }
        return data;
    }

    private String getIdentifier(IWSDIdentity<?> identity) {
        return String.format("%s:%s_%s", virtue.getModId(), identity.getPrefix(), identity.getIdentifier());
    }

}
