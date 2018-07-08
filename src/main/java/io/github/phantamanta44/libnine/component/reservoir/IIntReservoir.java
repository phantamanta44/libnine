package io.github.phantamanta44.libnine.component.reservoir;

import io.github.phantamanta44.libnine.util.data.ISerializable;
import io.github.phantamanta44.libnine.util.function.IIntBiConsumer;

public interface IIntReservoir extends ISerializable {

    int getQuantity();

    void setQuantity(int qty);

    default void offsetQuantity(int offset) {
        setQuantity(getQuantity() + offset);
    }

    int getCapacity();

    default int getRemainingCapacity() {
        return getCapacity() - getQuantity();
    }

    int draw(int amount, boolean notSimulated);

    int offer(int amount, boolean notSimulated);

    void onQuantityChange(IIntBiConsumer callback);

}
