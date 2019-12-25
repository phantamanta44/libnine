package io.github.phantamanta44.libnine.recipe;

import io.github.phantamanta44.libnine.recipe.input.IRcpIn;
import io.github.phantamanta44.libnine.recipe.output.IRcpOut;

public interface IRecipeManager {

    <T, I extends IRcpIn<T>, O extends IRcpOut<?>, R extends IRcp<T, I, O>> IRecipeList<T, I, O, R> getRecipeList(Class<R> type);

    void addType(Class<? extends IRcp<?, ?, ?>> type);
    
}
