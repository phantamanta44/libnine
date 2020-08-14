package io.github.phantamanta44.libnine.util.render.model;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collections;

public class NoopOverrideList extends ItemOverrideList {

    public static final NoopOverrideList INSTANCE = new NoopOverrideList();

    private NoopOverrideList() {
        super(Collections.emptyList());
    }

    @Override
    public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack,
                                       @Nullable World world, @Nullable EntityLivingBase entity) {
        return originalModel;
    }

}
