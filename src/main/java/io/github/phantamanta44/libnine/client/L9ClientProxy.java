package io.github.phantamanta44.libnine.client;

import io.github.phantamanta44.libnine.L9CommonProxy;
import io.github.phantamanta44.libnine.Registrar;
import io.github.phantamanta44.libnine.client.event.ClientTickHandler;
import io.github.phantamanta44.libnine.client.model.L9Models;
import io.github.phantamanta44.libnine.tile.L9TileEntity;
import io.github.phantamanta44.libnine.util.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class L9ClientProxy extends L9CommonProxy {

    /*
     * Internal
     */

    @Override
    protected Registrar initRegistrar() {
        return new ClientRegistrar();
    }

    /*
     * API
     */

    @Override
    public void dispatchTileUpdate(L9TileEntity tile) {
        if (!tile.getWorld().isRemote) super.dispatchTileUpdate(tile);
    }

    @Override
    public World getAnySidedWorld() {
        return FMLCommonHandler.instance().getEffectiveSide().isClient()
                ? Minecraft.getMinecraft().world : super.getAnySidedWorld();
    }

    @Nullable
    @Override
    public World getDimensionWorld(int dim) {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            World world = Minecraft.getMinecraft().world;
            return world.provider.getDimension() == dim ? world : null;
        } else {
            return super.getDimensionWorld(dim);
        }
    }

    /*
     * Callbacks
     */

    @Override
    protected void onPreInit(FMLPreInitializationEvent event) {
        RenderUtils.registerReloadHook();
        L9Models.registerModels();
        super.onPreInit(event);
        MinecraftForge.EVENT_BUS.register(new ClientTickHandler());
    }

    @Override
    protected void onInit(FMLInitializationEvent event) {
        super.onInit(event);
        getRegistrar().onRegisterColourHandlers();
    }

    @Override
    protected void onPostInit(FMLPostInitializationEvent event) {
        super.onPostInit(event);
        ClientCommandHandler.instance.registerCommand(new Command9S());
    }

    private static class Command9S implements ICommand {

        @Override
        public String getName() {
            return "9s";
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return "/9s <rs>";
        }

        @Override
        public List<String> getAliases() {
            return Collections.emptyList();
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            if (args.length == 1 && args[0].equals("rs")) {
                RenderUtils.reloadShaders();
                sender.sendMessage(new TextComponentString("ok"));
            } else {
                throw new CommandException("invalid");
            }
        }

        @Override
        public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
            return true;
        }

        @Override
        public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
            return Collections.emptyList();
        }

        @Override
        public boolean isUsernameIndex(String[] args, int index) {
            return false;
        }

        @Override
        public int compareTo(ICommand o) {
            return getName().compareTo(o.getName());
        }

    }

}
