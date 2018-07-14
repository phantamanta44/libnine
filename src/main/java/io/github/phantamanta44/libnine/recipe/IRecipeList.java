package io.github.phantamanta44.libnine.recipe;


import io.github.phantamanta44.libnine.recipe.input.IRcpIn;
import io.github.phantamanta44.libnine.recipe.output.IRcpOut;

import javax.annotation.Nullable;
import java.util.Collection;

public interface IRecipeList<T, I extends IRcpIn<T>, O extends IRcpOut, R extends IRcp<T, I, O>> {

    @Nullable
    R findRecipe(T input);

    Collection<R> recipes();

    void add(R recipe);

}
