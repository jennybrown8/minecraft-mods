package net.simsa.minecraftmods.go;

import net.minecraft.server.MinecraftServer;
import scala.actors.threadpool.Arrays;

public class MCUtil {

    public MCUtil() {
	// TODO Auto-generated constructor stub
    }


    public static boolean isOp(String player) {
	return Arrays.asList(MinecraftServer.getServer().getConfigurationManager().getOppedPlayerNames())
		.contains(player.toLowerCase().trim());
    }

    public static boolean isSinglePlayer() {
	return MinecraftServer.getServer().isSinglePlayer();
    }
}
