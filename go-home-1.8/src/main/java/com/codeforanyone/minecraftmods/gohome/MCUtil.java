package com.codeforanyone.minecraftmods.gohome;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class MCUtil {

    public MCUtil() {
    }


    public static boolean isOp(ICommandSender sender) {
	return sender.canUseCommand(MinecraftServer.getServer().getOpPermissionLevel(), "");
    }

    public static boolean isSinglePlayer() {
	return MinecraftServer.getServer().isSinglePlayer();
    }


    // TODO - Get this from the server somehow so it's cleaner.
    public static String dimensionName(int dimension) {
        switch (dimension) {
        case 0:
            return "Overworld";
        case -1:
            return "Nether";
        case 1:
            return "End";
        default:
            return "Non-Standard";
        }
    }
}
