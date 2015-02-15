package net.simsa.minecraftmods.go;

import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

@Mod(modid = GoMod.MODID, version = GoMod.VERSION)
public class GoMod {
    public static final String MODID = "go";
    public static final String VERSION = "1.0";

    @EventHandler
    public void init(FMLInitializationEvent event) {
	// Minecraft mc = Minecraft.getMinecraft();
	System.out.println("Go - Registering event listeners.");
	// player login event
	FMLCommonHandler.instance().bus().register(this);
	// world load event
	MinecraftForge.EVENT_BUS.register(this);
    }

    // register commands here
    @EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
	// Minecraft mc = Minecraft.getMinecraft();
	System.out.println("Go - Server Starting.");
	System.out.println("Go - World name is " + event.getServer().getWorldName());
	event.registerServerCommand(new CommandGo());
	System.out.println("Go - Registered CommandGo.");
    }


    @SubscribeEvent
    public void onLogin(PlayerLoggedInEvent event) {
	System.out.println("Go - Player logged in.");
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
	String worldname = event.world.getWorldInfo().getWorldName();
	World world = event.world;
	BlockPos spawnpoint = null;
	if (world != null) {
	    spawnpoint = world.getSpawnPoint();
	}
	System.out.println("Go - Spawn point for " + worldname + " is " + spawnpoint);
	// World spawn in current DevWorld: 451 64 -362,
    }

}
