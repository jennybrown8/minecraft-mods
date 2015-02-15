package net.simsa.minecraftmods.go;

import java.util.SortedSet;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import scala.actors.threadpool.Arrays;

/**
 * Primary command for the "go" mod; manages a list of named locations and lets
 * a user send themselves to a named location.
 * 
 */
public class CommandGo extends CommandBase {

    public CommandGo() {
    }

    @Override
    public String getName() {
	return "go";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
	return "/go to get a list, /go add locationname to add, /go rm locationname to delete, /go locationname to teleport. /go home is always world-spawn.";
    }

    public boolean isOp(String player) {
	return Arrays.asList(MinecraftServer.getServer().getConfigurationManager().getOppedPlayerNames())
		.contains(player.toLowerCase().trim());
    }

    public boolean isSinglePlayer() {
	return MinecraftServer.getServer().isSinglePlayer();
    }

    public String join(SortedSet<String> locations) {
	StringBuffer sb = new StringBuffer();
	for (String s : locations) {
	    sb.append(s);
	    sb.append("\n");
	}
	if (locations.size() == 0) {
	    sb.append("Use /go add locationname  to name where you stand as a teleport destination. /go home is reserved for spawn point.");
	}
	return sb.toString();
    }

    @Override
    public void execute(ICommandSender sender, String[] args) throws CommandException {
	NamedLocations namedLocations = NamedLocations.forWorld(sender.getEntityWorld());
	EntityPlayer player = (EntityPlayer) sender;
	System.out.println("Player " + player.getName() + " is running /go now.");
	System.out.println("Is player an op? " + isOp(player.getName()));
	
	// TODO - Handle operator-only locations and single-player mode
	// TODO - Handle safe-landing checks prior to teleporting
	// TODO - Handle cross-dimension teleport conditions
	
	if (args.length < 1) {
	    // print a list of options
	    SortedSet<String> names = namedLocations
		    .listPositions(isOp(player.getName()) || isSinglePlayer());
	    player.addChatComponentMessage(new ChatComponentText("Where to? Options:\n" + join(names)));
	} else if (args.length == 1) {
	    // go to the specified location if it's valid
	    BlockPos pos = namedLocations.getPosition(args[0]);
	    if (pos != null) {
		player.addChatComponentMessage(new ChatComponentText("Teleporting you to " + args[0] + " ("
			+ pos.getX() + "," + pos.getY() + "," + pos.getZ() + ")"));
		player.setPositionAndUpdate(pos.getX(), pos.getY(), pos.getZ());
	    } else {
		player.addChatComponentMessage(new ChatComponentText(args[0]
			+ " is not the name of any saved location."));
	    }
	} else if (args.length == 2 && args[0].equals("add")) {
	    // add a location
	    namedLocations.savePosition(args[1], player.getPosition(), false);
	    player.addChatComponentMessage(new ChatComponentText("Added location " + args[1] + " as ("
		    + player.getPosition().getX() + "," + player.getPosition().getY() + ","
		    + player.getPosition().getZ() + ")"));
	} else if (args.length == 2 && args[0].equals("rm")) {
	    // delete a location
	    namedLocations.deletePosition(args[1]);
	    player.addChatComponentMessage(new ChatComponentText("Removed location " + args[1]));
	}
    }

}
