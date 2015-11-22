package com.codeforanyone.minecraftmods.go;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = GoMod.MODID, version = GoMod.VERSION, name = GoMod.NAME)
public class GoMod {
	public static final String MODID = "go";
	public static final String VERSION = "1.1";
	public static final String NAME = "Go";

	private CommandGo gocommand;

	@EventHandler
	public void init(FMLInitializationEvent event) {
		System.out.println("Go - Registering event listeners.");

		// player login event
		FMLCommonHandler.instance().bus().register(this);
		// world load event
		MinecraftForge.EVENT_BUS.register(this);
	}

	// register commands here
	@EventHandler
	public void onServerStarting(FMLServerStartingEvent event) {
		System.out.println("Go - registering /go command for world " + event.getServer().getWorldName());
		gocommand = new CommandGo();
		event.registerServerCommand(gocommand);
	}

	// @formatter:off
	/*
	 * @SubscribeEvent public void onLogin(PlayerLoggedInEvent event) {
	 * System.out.println("Go - Player logged in."); }
	 */

	/*
	 * @SubscribeEvent public void onWorldLoad(WorldEvent.Load event) { if
	 * (event.world == null) { return; } String worldname =
	 * event.world.getWorldInfo().getWorldName(); }
	 */
	// @formatter:on

}
