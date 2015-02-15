package net.simsa.minecraftmods.go;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

@Mod(modid = GoMod.MODID, version = GoMod.VERSION, name = GoMod.NAME)
public class GoMod {
    public static final String MODID = "go";
    public static final String VERSION = "1.0";
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
	System.out.println("Go - Server Starting.");
	System.out.println("Go - World name is " + event.getServer().getWorldName());
	gocommand = new CommandGo();
	event.registerServerCommand(gocommand);
	System.out.println("Go - Registered CommandGo.");
    }

    @SubscribeEvent
    public void onLogin(PlayerLoggedInEvent event) {
	System.out.println("Go - Player logged in.");
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
	if (event.world == null) {
	    return;
	}
	//MyWorldData.forWorld(event.world).readFromNBT(nbt);
	String worldname = event.world.getWorldInfo().getWorldName();
	System.out.println("Go - Spawn point for " + worldname + " is " + event.world.getSpawnPoint());
	// World spawn in current DevWorld: 451 64 -362,
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
