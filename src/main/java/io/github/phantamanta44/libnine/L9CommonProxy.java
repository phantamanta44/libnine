package io.github.phantamanta44.libnine;

import io.github.phantamanta44.libnine.network.PacketServerSyncTileEntity;
import io.github.phantamanta44.libnine.recipe.IRecipeList;
import io.github.phantamanta44.libnine.recipe.IRecipeManager;
import io.github.phantamanta44.libnine.recipe.RecipeManager;
import io.github.phantamanta44.libnine.recipe.input.ItemStackInput;
import io.github.phantamanta44.libnine.recipe.output.ItemStackOutput;
import io.github.phantamanta44.libnine.recipe.type.SmeltingRecipe;
import io.github.phantamanta44.libnine.tile.L9TileEntity;
import io.github.phantamanta44.libnine.tile.RegisterTile;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.asm.ModAnnotation;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

import java.util.List;

public class L9CommonProxy {

    private final Registrar registrar;
    private final RecipeManager recipeManager;

    public L9CommonProxy() {
        registrar = initRegistrar();
        recipeManager = initRecipeManager();
    }

    /*
     * Internal
     */

    protected Registrar initRegistrar() {
        return new Registrar();
    }

    @SuppressWarnings("WeakerAccess")
    protected RecipeManager initRecipeManager() {
        return new RecipeManager();
    }

    /*
     * API
     */

    public Registrar getRegistrar() {
        return registrar;
    }

    public IRecipeManager getRecipeManager() {
        return recipeManager;
    }

    public void dispatchTileUpdate(L9TileEntity tile) {
        BlockPos pos = tile.getPos();
        getRegistrar().lookUpTileVirtue(tile.getClass()).getNetworkHandler().sendToAllAround(
                new PacketServerSyncTileEntity(tile),
                new NetworkRegistry.TargetPoint(
                        tile.getWorld().provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 64D));
    }

    public World getAnySidedWorld() {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld();
    }

    /*
     * Callbacks
     */

    protected void onPreInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(registrar);
        for (ASMDataTable.ASMData target : event.getAsmData().getAll(RegisterTile.class.getName())) {
            getRegistrar().queueTileEntityReg((String)target.getAnnotationInfo().get("value"), target.getClassName());
        }
        Side actualSide = FMLCommonHandler.instance().getSide();
        for (ASMDataTable.ASMData target : event.getAsmData().getAll(InitMe.class.getName())) {
            List sides = (List)target.getAnnotationInfo().get("sides");
            boolean shouldContinue = true;
            if (sides != null) {
                shouldContinue = false;
                for (Object side : sides) {
                    if (actualSide == Side.valueOf(((ModAnnotation.EnumHolder)side).getValue())) {
                        shouldContinue = true;
                    }
                }
            }
            if (shouldContinue) {
                String modId = (String)target.getAnnotationInfo().get("value");
                boolean bind = modId != null && !modId.isEmpty();
                if (bind) getRegistrar().begin(Virtue.forMod(modId));
                try {
                    String methodName = target.getObjectName();
                    methodName = methodName.substring(0, methodName.lastIndexOf('('));
                    Class.forName(target.getClassName()).getDeclaredMethod(methodName).invoke(null);
                } catch (Exception e) {
                    LibNine.LOGGER.error("Failed to run initializer {}::{} for virtue {}",
                            target.getClassName(), target.getObjectName(), modId);
                    LibNine.LOGGER.error("", e);
                }
                if (bind) getRegistrar().end();
            }
        }
    }

    protected void onInit(FMLInitializationEvent event) {
        // NO-OP
    }

    protected void onPostInit(FMLPostInitializationEvent event) {
        // NO-OP
    }

    @SuppressWarnings("WeakerAccess")
    protected void onLoadComplete(FMLLoadCompleteEvent event) {
        IRecipeList<ItemStack, ItemStackInput, ItemStackOutput, SmeltingRecipe> smeltingRecipes
                = recipeManager.getRecipeList(SmeltingRecipe.class);
        FurnaceRecipes.instance().getSmeltingList().forEach((i, o) -> smeltingRecipes.add(new SmeltingRecipe(i, o)));
    }

}
