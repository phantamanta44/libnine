package io.github.phantamanta44.libnine.block.state;

import com.google.common.collect.Lists;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VirtualState {

    private final Map<IProperty<?>, Comparable<?>> props;

    private VirtualState() {
        this.props = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getSerializedProperties() {
        Map<String, String> result = new HashMap<>();
        props.forEach((p, v) -> result.put(p.getName(), ((IProperty)p).getName(v)));
        return result;
    }

    @SuppressWarnings("unchecked")
    public <T extends Comparable<T>> T get(IProperty<T> prop) {
        return (T)props.get(prop);
    }

    public boolean matches(IBlockState state) {
        for (Map.Entry<IProperty<?>, Comparable<?>> prop : props.entrySet()) {
            if (!state.getProperties().get(prop.getKey()).equals(prop.getValue())) return false;
        }
        return true;
    }

    @SuppressWarnings({"unchecked", "RedundantCast"})
    public IBlockState synthesize(BlockStateContainer container) {
        IBlockState state = container.getBaseState();
        for (Map.Entry<IProperty<?>, Comparable<?>> prop : props.entrySet()) {
            state = state.withProperty((IProperty)prop.getKey(), (Comparable)prop.getValue());
        }
        return state;
    }

    private Stream<VirtualState> cartesian(IProperty<?> prop) {
        return prop.getAllowedValues().stream().map(v -> compose(prop, v));
    }

    private VirtualState compose(IProperty<?> prop, Comparable value) {
        VirtualState result = new VirtualState();
        result.props.putAll(props);
        result.props.put(prop, value);
        return result;
    }

    private VirtualState compose(VirtualState other) {
        VirtualState result = new VirtualState();
        result.props.putAll(props);
        result.props.putAll(other.props);
        return result;
    }

    public static List<VirtualState> cartesian(List<IProperty<?>> props) {
        return props.stream().<List<VirtualState>>reduce(Lists.newArrayList(new VirtualState()),
                (l, p) -> l.stream().flatMap(s -> s.cartesian(p)).collect(Collectors.toList()),
                (a, b) -> a.stream().flatMap(s -> b.stream().map(s::compose)).collect(Collectors.toList()));
    }

}
